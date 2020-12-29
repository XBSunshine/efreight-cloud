package com.efreight.afbase.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DebitNoteSendEntity {
    private String receiver;
    private String bccUser;
    private String ccUser;
    private String subject;
    private String content;
    private String templateType;
    private String debitNoteId;
    private String orderFileIds;
    private String orgName;
    private String businessScope;

}
