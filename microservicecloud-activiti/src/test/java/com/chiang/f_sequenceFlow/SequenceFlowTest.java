package com.chiang.f_sequenceFlow;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import com.chiang.activiti.bean.Person;

public class SequenceFlowTest {

	ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

	/***
	 * activiti初始化数据库
	 */
	@Test
	public void initDatabase() {
		// 1、创建ProcessEngine配置对象
		ProcessEngineConfiguration configuration = ProcessEngineConfiguration
				.createStandaloneProcessEngineConfiguration();
		// 2、做配置
		// mysql链接后加“&nullCatalogMeansCurrent=true”
		configuration.setJdbcUrl(
				"jdbc:mysql://localhost:3306/activiti?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&characterEncoding=UTF-8&useSSL=true&nullCatalogMeansCurrent=true");
		configuration.setJdbcDriver("com.mysql.cj.jdbc.Driver");
		configuration.setJdbcUsername("root");
		configuration.setJdbcPassword("success");

		// 配置建表策略
		configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

		// 3、创建ProcessEngine
		ProcessEngine processEngine = configuration.buildProcessEngine();

		System.out.println("processEngine" + processEngine);
	}

	/** 部署流程定义（从InputStream） */
	@Test
	public void deploymentProcessDefinition_inputStream() {
		InputStream inputStreamBpmn = this.getClass().getResourceAsStream("sequenceFlow.bpmn");
		InputStream inputStreamPng = this.getClass().getResourceAsStream("sequenceFlow.bpmn");
		Deployment deployment = processEngine.getRepositoryService()// 与流程定义和部署对象相关的Service
				.createDeployment()// 创建一个部署对象
				.name("连线")// 添加部署的名称
				.addInputStream("sequenceFlow.bpmn", inputStreamBpmn)
				.deploy();// 完成部署
		System.out.println("部署ID：" + deployment.getId());//
		System.out.println("部署名称：" + deployment.getName());//
	}

	/** 启动流程实例 */
	@Test
	public void startProcessInstance() {
		// 流程定义的key
		String processDefinitionKey = "sequenceFlow";
		ProcessInstance pi = processEngine.getRuntimeService()// 与正在执行的流程实例和执行对象相关的Service
				.startProcessInstanceByKey(processDefinitionKey);// 使用流程定义的key启动流程实例，key对应helloworld.bpmn文件中id的属性值，使用key值启动，默认是按照最新版本的流程定义启动
				//.startProcessInstanceById("sequenceFlow:1:4");// 使用流程定义的key启动流程实例，key对应helloworld.bpmn文件中id的属性值，使用key值启动，默认是按照最新版本的流程定义启动
		System.out.println("流程实例ID:" + pi.getId());// 流程实例ID
		System.out.println("流程定义ID:" + pi.getProcessDefinitionId());// 流程定义ID
	}

	@Test
	public void findMyPersonalTask() {
		String assignee = "赵六";
		List<Task> list = processEngine.getTaskService()// 与正在执行的任务管理相关的Service
				.createTaskQuery()// 创建任务查询对象
				/** 查询条件（where部分） */
				.taskAssignee(assignee)// 指定个人任务查询，指定办理人
				// .taskCandidateUser(candidateUser)//组任务的办理人查询
				// .processDefinitionId(processDefinitionId)//使用流程定义ID查询
				// .processInstanceId(processInstanceId)//使用流程实例ID查询
				// .executionId(executionId)//使用执行对象ID查询
				/** 排序 */
				.orderByTaskCreateTime().asc()// 使用创建时间的升序排列
				/** 返回结果集 */
				// .singleResult()//返回惟一结果集
				// .count()//返回结果集的数量
				// .listPage(firstResult, maxResults);//分页查询
				.list();// 返回列表
		
		if(list!=null && list.size()>0){
			for(Task task:list){
				System.out.println("任务ID:"+task.getId());
				System.out.println("任务名称:"+task.getName());
				System.out.println("任务的创建时间:"+task.getCreateTime());
				System.out.println("任务的办理人:"+task.getAssignee());
				System.out.println("流程实例ID："+task.getProcessInstanceId());
				System.out.println("执行对象ID:"+task.getExecutionId());
				System.out.println("流程定义ID:"+task.getProcessDefinitionId());
				System.out.println("########################################################");
			}
		}
		
	}

	/** 设置流程变量 */
	@Test
	public void setVariables() {
		/** 与任务（正在执行） */
		TaskService taskService = processEngine.getTaskService();
		// 任务ID
		String taskId = "5004";
		/** 一：设置流程变量，使用基本数据类型 */
		// taskService.setVariableLocal(taskId, "请假天数", 5);//与任务ID绑定
		// taskService.setVariable(taskId, "请假日期", new Date());
		// taskService.setVariable(taskId, "请假原因", "回家探亲，一起吃个饭");
		/** 二：设置流程变量，使用javabean类型 */
		/**
		 * 当一个javabean（实现序列号）放置到流程变量中，要求javabean的属性不能再发生变化 * 如果发生变化，再获取的时候，抛出异常
		 * 
		 * 解决方案：在Person对象中添加： private static final long serialVersionUID =
		 * 6757393795687480331L; 同时实现Serializable
		 */
		Person p = new Person();
		p.setId(20);
		p.setName("翠花");
		taskService.setVariable(taskId, "人员信息(添加固定版本)", p);

		System.out.println("设置流程变量成功！");
	}

	/** 获取流程变量 */
	@Test
	public void getVariables() {
		/** 与任务（正在执行） */
		TaskService taskService = processEngine.getTaskService();
		// 任务ID
		String taskId = "2104";
		/** 一：获取流程变量，使用基本数据类型 */
		// Integer days = (Integer) taskService.getVariable(taskId, "请假天数");
		// Date date = (Date) taskService.getVariable(taskId, "请假日期");
		// String resean = (String) taskService.getVariable(taskId, "请假原因");
		// System.out.println("请假天数："+days);
		// System.out.println("请假日期："+date);
		// System.out.println("请假原因："+resean);
		/** 二：获取流程变量，使用javabean类型 */
		Person p = (Person) taskService.getVariable(taskId, "人员信息(添加固定版本)");
		System.out.println(p.getId() + "        " + p.getName());
	}

	/** 模拟设置和获取流程变量的场景 */
	public void setAndGetVariables() {
		/** 与流程实例，执行对象（正在执行） */
		RuntimeService runtimeService = processEngine.getRuntimeService();
		/** 与任务（正在执行） */
		TaskService taskService = processEngine.getTaskService();

		/** 设置流程变量 */
		// runtimeService.setVariable(executionId, variableName,
		// value)//表示使用执行对象ID，和流程变量的名称，设置流程变量的值（一次只能设置一个值）
		// runtimeService.setVariables(executionId,
		// variables)//表示使用执行对象ID，和Map集合设置流程变量，map集合的key就是流程变量的名称，map集合的value就是流程变量的值（一次设置多个值）

		// taskService.setVariable(taskId, variableName,
		// value)//表示使用任务ID，和流程变量的名称，设置流程变量的值（一次只能设置一个值）
		// taskService.setVariables(taskId,
		// variables)//表示使用任务ID，和Map集合设置流程变量，map集合的key就是流程变量的名称，map集合的value就是流程变量的值（一次设置多个值）

		// runtimeService.startProcessInstanceByKey(processDefinitionKey,
		// variables);//启动流程实例的同时，可以设置流程变量，用Map集合
		// taskService.complete(taskId, variables)//完成任务的同时，设置流程变量，用Map集合

		/** 获取流程变量 */
		// runtimeService.getVariable(executionId,
		// variableName);//使用执行对象ID和流程变量的名称，获取流程变量的值
		// runtimeService.getVariables(executionId);//使用执行对象ID，获取所有的流程变量，将流程变量放置到Map集合中，map集合的key就是流程变量的名称，map集合的value就是流程变量的值
		// runtimeService.getVariables(executionId,
		// variableNames);//使用执行对象ID，获取流程变量的值，通过设置流程变量的名称存放到集合中，获取指定流程变量名称的流程变量的值，值存放到Map集合中

		// taskService.getVariable(taskId, variableName);//使用任务ID和流程变量的名称，获取流程变量的值
		// taskService.getVariables(taskId);//使用任务ID，获取所有的流程变量，将流程变量放置到Map集合中，map集合的key就是流程变量的名称，map集合的value就是流程变量的值
		// taskService.getVariables(taskId,
		// variableNames);//使用任务ID，获取流程变量的值，通过设置流程变量的名称存放到集合中，获取指定流程变量名称的流程变量的值，值存放到Map集合中

	}

	/** 完成我的任务 */
	@Test
	public void completeMyPersonalTask() {
		// 任务ID
		String taskId = "30003";
		//完成任务的同时设置流程变量，使用流程变量来指定完成任务后，下一个连线，对应sequenceFlow.bpmn文件中#{message=='不重要'}
		Map<String,Object> variables = new HashMap<String,Object>();
		variables.put("message", "重要");
		processEngine.getTaskService()// 与正在执行的任务管理相关的Service
				.complete(taskId,variables);
		System.out.println("完成任务：任务ID：" + taskId);
	}

	/** 查询流程变量的历史表 */
	@Test
	public void findHistoryProcessVariables() {
		List<HistoricVariableInstance> list = processEngine.getHistoryService()//
				.createHistoricVariableInstanceQuery()// 创建一个历史的流程变量查询对象
				.variableName("请假天数").list();
		if (list != null && list.size() > 0) {
			for (HistoricVariableInstance hvi : list) {
				System.out.println(hvi.getId() + "   " + hvi.getProcessInstanceId() + "   " + hvi.getVariableName()
						+ "   " + hvi.getVariableTypeName() + "    " + hvi.getValue());
				System.out.println("###############################################");
			}
		}
	}

}
