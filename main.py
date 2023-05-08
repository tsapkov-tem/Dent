import sys
import os
import pickle5 as pickle
import numpy as np
import matplotlib.pyplot as plt
from PIL import Image, ImageOps
import torch
from py4j.clientserver import ClientServer, JavaParameters, PythonParameters
from transformers import GLPNImageProcessor, GLPNForDepthEstimation
import open3d as o3d
from py4j.java_gateway import JavaGateway

#Сообщаем Керас что наш бэкэне тензорфлов
os.environ["KERAS_BACKEND"] = "tensorflow"

np.random.seed(10)
random_dim = 100
np.set_printoptions(threshold=sys.maxsize)

absolute_path_to_generated_img_folder = "C:/Users/tsapk/IdeaProjects/Dent/src/main/resources/static/fissureResult/"
absolute_path_to_uploaded_img_folder = "C:/Users/tsapk/IdeaProjects/Dent/files/"
absolute_path_to_mesh_folder = "C:/Users/tsapk/IdeaProjects/Dent/mesh/"

#для генерации 3д моделей
feature_extractor = GLPNImageProcessor.from_pretrained("vinvino02/glpn-nyu")
model = GLPNForDepthEstimation.from_pretrained("vinvino02/glpn-nyu")


def generated_images(result_id, generator, examples=1, dim=(1, 1), figsize=(5, 5)):
    noise = np.random.normal(0, 1, size=[examples, random_dim])
    generated_images = generator.predict(noise)
    generated_images = generated_images.reshape(examples, 448, 448)
    plt.figure(figsize=figsize)
    for i in range(generated_images.shape[0]):
        plt.subplot(dim[0], dim[1], i + 1)
        plt.imshow(generated_images[i], interpolation="nearest", cmap='gray_r')
        plt.axis("off")
    plt.tight_layout()

    fissure_filename = absolute_path_to_generated_img_folder + result_id + '_2d_fissure.jpg'
    plt.savefig(fissure_filename)
    return fissure_filename

def get_changed_depth(predicted_depth, change_depth, size_h, size_w):
    if predicted_depth.shape == change_depth.shape:
        depth_range = 0
        count = 0
        with torch.no_grad():
            for i in range(0, size_h):
                for j in range(0, size_w):
                    if(predicted_depth[0, i, j] < 10 and change_depth[0, i, j] < 10):
                        depth_range += predicted_depth[0, i, j] + change_depth[0, i, j]
                        count += 1
        depth_range /= count
        with torch.no_grad():
            for i in range(0, size_h):
                for j in range(0, size_w):
                    if predicted_depth[0, i, j] < 10 and change_depth[0, i, j] < 10:
                        overlap = abs(predicted_depth[0, i, j] - change_depth[0, i, j])
                        emptiness = depth_range - (predicted_depth[0, i, j] + change_depth[0, i, j])
                        if overlap > (depth_range * 0.3):
                            print('overlap')
                            predicted_depth[0, i, j] -= overlap
                        if emptiness > depth_range * 0.3:
                            print('emptiness')
                            predicted_depth[0, i, j] += emptiness * 0.5
    return predicted_depth

def prepare_img(filename, generated_img=False, dent_high=30, change_depth=None):
    # load and resize the input image
    image = Image.open(filename)
    new_height = 480 if image.height > 480 else image.height
    new_height -= (new_height % 32)
    new_width = int(new_height * image.width / image.height)
    diff = new_width % 32
    new_width = new_width - diff if diff < 16 else new_width + 32 - diff
    new_size = (new_width, new_height)
    image = image.resize(new_size)
    image.show()
    if (generated_img):
        image = ImageOps.invert(image)
        image.show()
    inputs = feature_extractor(images=image, return_tensors="pt")
    # get the prediction from the model
    with torch.no_grad():
        outputs = model(**inputs)
        predicted_depth = outputs.predicted_depth
        rgb_im = image.convert('RGB')
        for i in range(0, new_height):
            for j in range(0, new_width):
                r, g, b = rgb_im.getpixel((j, i))
                if r < 50 and g < 50 and b < 50:
                    predicted_depth[0, i, j] = dent_high

    # remove borders
    pad = 16
    image = image.crop((pad, pad, image.width - pad, image.height - pad))
    width, height = image.size
    if (generated_img):
        predicted_depth = get_changed_depth(predicted_depth, change_depth, height, width)
    output = predicted_depth.squeeze().cpu().numpy() * 1000.0
    output = output[pad:-pad, pad:-pad]
    depth_image = (output * 255 / np.max(output)).astype('uint8')
    image = np.array(image)
    # create rgbd image
    depth_o3d = o3d.geometry.Image(depth_image)
    image_o3d = o3d.geometry.Image(image)
    rgbd_image = o3d.geometry.RGBDImage.create_from_color_and_depth(image_o3d, depth_o3d,
                                                                    convert_rgb_to_intensity=False)
    return width, height, predicted_depth, rgbd_image;


def get_dent_height(neighbor_filename):
    width, height, predicted_depth, _ = prepare_img(neighbor_filename)
    high_dot = -1
    with torch.no_grad():
        for i in range(0, height):
            for j in range(0, width):
                if predicted_depth[0, i, j] < 10:
                    if high_dot == -1:
                        high_dot = i
                        break
                    else:
                        low_dot = i
                        break

    dent_height = (low_dot - high_dot)
    return dent_height


def mesh_generate(fissure_filename, antagonist_filename, dent_height, id):
    # prepare image for the model
    _, _, antagonist_depth_map, _ = prepare_img(antagonist_filename)
    width, height, _, rgbd_image = prepare_img(fissure_filename, True, dent_height, antagonist_depth_map)

    # camera settings
    camera_intrinsic = o3d.camera.PinholeCameraIntrinsic()
    camera_intrinsic.set_intrinsics(width, height, 500, 500, width / 2, height / 2)
    # create point cloud
    pcd = o3d.geometry.PointCloud.create_from_rgbd_image(rgbd_image, camera_intrinsic)
    points = np.asarray(pcd.points)
    # outliers removal
    cl, ind = pcd.remove_statistical_outlier(nb_neighbors=20, std_ratio=20.0)
    pcd = pcd.select_by_index(ind)
    # estimate normals
    pcd.estimate_normals()
    pcd.orient_normals_to_align_with_direction()
    # surface reconstruction
    mesh = o3d.geometry.TriangleMesh.create_from_point_cloud_poisson(pcd, depth=10, n_threads=1)[0]
    print(mesh)
    # rotate the mesh
    rotation = mesh.get_rotation_matrix_from_xyz((np.pi, 0, 0))
    mesh.rotate(rotation, center=(0, 0, 0))
    mesh1 = mesh
    mesh1.triangles = o3d.utility.Vector3iVector(
        np.asarray(mesh1.triangles)[:len(mesh1.triangles) // dent_height, :])
    mesh1.triangle_normals = o3d.utility.Vector3dVector(
        np.asarray(mesh1.triangle_normals)[:len(mesh1.triangle_normals) // dent_height, :])
    # cleaning
    mesh1 = mesh1.simplify_quadric_decimation(10000)
    mesh1.remove_unreferenced_vertices
    mesh1.remove_degenerate_triangles()
    mesh1.remove_duplicated_triangles()
    mesh1.remove_duplicated_vertices()
    mesh1.remove_non_manifold_edges()
    path = absolute_path_to_mesh_folder + id + 'mesh.obj'
    # save the mesh
    o3d.io.write_triangle_mesh(path, mesh1)


class Operator():
    def get_fissure(self, id):
        print("getfis()" + id)
        with open("model.pkl", 'rb') as gan:  # Overwrites any existing file.
            generator = pickle.load(gan)
        return generated_images(id, generator)

    def get_mesh(self, id):
        print("getmesh()" + id)
        absolute_path_to_gen_img = absolute_path_to_generated_img_folder + id + '_2d_fissure.jpg'
        path_to_neighbors_img = absolute_path_to_uploaded_img_folder + id + "neighbors.jpg"
        path_to_antagonist_img = absolute_path_to_uploaded_img_folder + id + "fissure.jpg"
        dent_z = get_dent_height(path_to_neighbors_img)
        mesh_generate(absolute_path_to_gen_img, path_to_antagonist_img, dent_z + dent_z // 10, id)

    class Java:
        implements = ['com.example.dent.Services.Operator']

if __name__ == '__main__':
    # gateway = JavaGateway(start_callback_server=True)  # Открываем порт с Java
    # entry_point = gateway.entry_point
    # entry_point.fromPython("Ответ от Python!")
    operator = Operator()
    gateway = ClientServer(
        java_parameters=JavaParameters(),
        python_parameters=PythonParameters(),
        python_server_entry_point=operator)

