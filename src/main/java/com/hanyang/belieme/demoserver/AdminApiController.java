package com.hanyang.belieme.demoserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

;
import java.util.Optional;

@RestController
@RequestMapping(path="/admin")
public class AdminApiController {
    @Autowired
    private AdminRepository adminRepository;

    @GetMapping("/")
    public ResponseWrapper<Iterable<Admin>> getAdminList() {
        return new ResponseWrapper<>(ResponseHeader.OK, adminRepository.findAll());
    }

    @PostMapping("/")
    public ResponseWrapper<Iterable<Admin>> postNewAdmin(@RequestBody Admin admin) {
        if(admin.getPermission().equals("admin")) {
            admin.permissionSetAdmin();
        } else if(admin.getPermission().equals("master")) {
            admin.permissionSetMaster();
        } else if(admin.getPermission().equals("developer")) {
            admin.permissionSetDeveloper();
        } else {
            return new ResponseWrapper<>(ResponseHeader.WRONG_ADMIN_PERMISSION_EXCEPTION, null);
        }
        adminRepository.save(admin);

        return new ResponseWrapper<>(ResponseHeader.OK, adminRepository.findAll());
    }

    @PutMapping("/setPermissionAdmin/{studentId}")
    public ResponseWrapper<Iterable<Admin>> setPermissionAdmin(@PathVariable int studentId) {
        Optional<Admin> target = adminRepository.findById(studentId);
        if(target.isPresent()) {
            target.get().permissionSetAdmin();
            adminRepository.save(target.get());
            return new ResponseWrapper<>(ResponseHeader.OK, adminRepository.findAll());
        } else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }

    @PutMapping("/setPermissionMaster/{studentId}")
    public ResponseWrapper<Iterable<Admin>> setPermissionMaster(@PathVariable int studentId) {
        Optional<Admin> target = adminRepository.findById(studentId);
        if(target.isPresent()) {
            target.get().permissionSetMaster();
            adminRepository.save(target.get());
            return new ResponseWrapper<>(ResponseHeader.OK, adminRepository.findAll());
        } else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }

    @PutMapping("/setPermissionDeveloper/{studentId}")
    public ResponseWrapper<Iterable<Admin>> setPermissionDeveloper(@PathVariable int studentId) {
        Optional<Admin> target = adminRepository.findById(studentId);
        if(target.isPresent()) {
            target.get().permissionSetDeveloper();
            adminRepository.save(target.get());
            return new ResponseWrapper<>(ResponseHeader.OK, adminRepository.findAll());
        } else {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        }
    }

    @DeleteMapping("{studentId}")
    public ResponseWrapper<Iterable<Admin>> deleteAdmin(@PathVariable int studentId) {
        adminRepository.deleteById(studentId);
        return new ResponseWrapper<>(ResponseHeader.OK, adminRepository.findAll());
    }
}
