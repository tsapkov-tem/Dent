import sys

import matplotlib
import open3d

matplotlib.use('TkAgg')
from matplotlib import pyplot as plt
from PIL import Image
import torch
from transformers import GLPNImageProcessor, GLPNForDepthEstimation
import numpy as np
import open3d as o3d
np.set_printoptions(threshold=sys.maxsize)

feature_extractor = GLPNImageProcessor.from_pretrained("vinvino02/glpn-nyu")
model = GLPNForDepthEstimation.from_pretrained("vinvino02/glpn-nyu")

# load and resize the input image
image = Image.open("imageFales/(1)antagonist.jpg")

new_height = 480 if image.height > 480 else image.height
new_height -= (new_height % 32)
new_width = int(new_height * image.width / image.height)
diff = new_width % 32
new_width = new_width - diff if diff < 16 else new_width + 32 - diff
new_size = (new_width, new_height)
image = image.resize(new_size)


# prepare image for the model
inputs = feature_extractor(images=image, return_tensors="pt")

# get the prediction from the model
with torch.no_grad():
    outputs = model(**inputs)
    predicted_depth = outputs.predicted_depth
    rgb_im = image.convert('RGB')
    for i in range(0, 480):
        for j in range(0, 480):
            r, g, b = rgb_im.getpixel((j, i))
            if r == 0 and g == 0 and b == 0:
                predicted_depth[0, i, j] = 10

# remove borders
pad = 16
output = predicted_depth.squeeze().cpu().numpy() * 1000.0
output = output[pad:-pad, pad:-pad]
image = image.crop((pad, pad, image.width - pad, image.height - pad))


width, height = image.size
depth_image = (output * 255 / np.max(output)).astype('uint8')

print(depth_image.shape)
# depth_image = np.genfromtxt("weights/example1.txt", dtype=np.uint8)
print(depth_image.shape)
image = np.array(image)

print(image.shape)


# create rgbd image
depth_o3d = o3d.geometry.Image(depth_image)
image_o3d = o3d.geometry.Image(image)
rgbd_image = o3d.geometry.RGBDImage.create_from_color_and_depth(image_o3d, depth_o3d,  convert_rgb_to_intensity=False)
print("rgbd_image")

# camera settings
camera_intrinsic = o3d.camera.PinholeCameraIntrinsic()
camera_intrinsic.set_intrinsics(width, height, 500, 500, width/2, height/2)

# create point cloud
pcd = o3d.geometry.PointCloud.create_from_rgbd_image(rgbd_image, camera_intrinsic)
print("pcd")

points = np.asarray(pcd.points)
print(len(points))

# outliers removal
cl, ind = pcd.remove_statistical_outlier(nb_neighbors=5, std_ratio=5.0)
pcd = pcd.select_by_index(ind)

# estimate normals
pcd.estimate_normals()
pcd.orient_normals_to_align_with_direction()
print("normals")


# surface reconstruction
mesh = o3d.geometry.TriangleMesh.create_from_point_cloud_poisson(pcd, depth=15, n_threads=1)[0]
print("mesh")
# rotate the mesh
rotation = mesh.get_rotation_matrix_from_xyz((np.pi, 0, 0))
mesh.rotate(rotation, center=(0, 0, 0))
print("rotation")
# save the mesh
o3d.io.write_triangle_mesh(f'./mesh_(1)dent.obj', mesh)