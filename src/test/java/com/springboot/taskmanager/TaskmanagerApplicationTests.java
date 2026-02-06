package com.springboot.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TaskmanagerApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void mainMethodStartsApplication() {
		TaskmanagerApplication.main(new String[] {});
	}
}
