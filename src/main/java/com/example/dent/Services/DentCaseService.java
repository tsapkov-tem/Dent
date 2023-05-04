package com.example.dent.Services;

import com.example.dent.Models.DentCase;
import com.example.dent.Repository.DentCaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DentCaseService {

    private final DentCaseRepository dentCaseRepository;

    @Autowired
    public DentCaseService(DentCaseRepository dentCaseRepository) {
        this.dentCaseRepository = dentCaseRepository;
    }

    public DentCase saveCase(String id_case){
        DentCase dentCase = new DentCase();
        dentCase.setId_case(id_case);
        dentCaseRepository.save(dentCase);
        return dentCase;
    }

    public DentCase updateCase(String id_case, String material, String info){
        DentCase dentCase = dentCaseRepository.findById(id_case).orElse(null);
        try {
            dentCase.setMaterial(material);
            dentCase.setInfo(info);
            dentCaseRepository.save(dentCase);
        }catch (NullPointerException e){
            System.out.println(e.getMessage());
            return null;
        }
        return dentCase;
    }
}
