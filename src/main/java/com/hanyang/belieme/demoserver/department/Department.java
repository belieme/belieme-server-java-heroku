package com.hanyang.belieme.demoserver.department;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Department {
    @Id
    @Column(name = "major_id", nullable = false)
    private String majorId;

    @Column(name = "major_name", nullable = false)
    private String majorName;
    
    //TODO 한양 API정보 추가
}
