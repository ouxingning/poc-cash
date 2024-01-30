package com.xnou.demo.cash.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author OU Xingning
 * @date 2024/01/30
 */
@RestController
@RequestMapping("/rest")
public class HealthCheckController {

	@RequestMapping("/health-check")
	public String healthCheck() {
		return "ok";
	}

}
