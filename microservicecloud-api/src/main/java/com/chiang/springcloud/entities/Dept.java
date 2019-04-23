package com.chiang.springcloud.entities;

import java.io.Serializable;

import org.apache.http.impl.execchain.MainClientExec;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@SuppressWarnings({ "unused", "serial" })
// @AllArgsConstructor
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Dept implements Serializable { // Entity orm
	/**
	 * 
	 */
	private Long deptno; // 主键
	private String dname; // 部门名称
	private String db_source;// 来自那个数据库，因为微服务架构可以一个服务对应一个数据库，同一个信息被存储到不同数据库

	
	public static void main(String[]args) {
		Dept dpt = new Dept();
		dpt.setDeptno(11L).setDname("RD").setDb_source("DB01");
	}
}
