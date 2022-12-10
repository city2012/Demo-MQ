package com.yun.practice.midware.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public abstract class AuditEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * record create time
     */
    @TableField(value = "db_create_time")
    private Date dbCreateTime;

    /**
     * record update time
     */
    @TableField(value = "db_modify_time")
    private Date dbModifyTime;

}
