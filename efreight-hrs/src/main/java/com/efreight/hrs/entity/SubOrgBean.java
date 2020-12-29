package com.efreight.hrs.entity;

import lombok.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SubOrgBean {

    /**
     * 父企业ID
     */
    private Integer orgId;

    /**
     * 用户ID
     */
    private List<Org> selectionSubOrgs;

}
