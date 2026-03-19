package com.gd.self.tool;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableKnife4j
@EnableScheduling
@MapperScan
public class MyToolApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyToolApplication.class, args);
		System.out.println("""
                my-tool 启动成功
                ___  ____   __  _____ _____  _____ _       _____ _____ ___  ______ _____ ___________\s
                |  \\/  \\ \\ / / |_   _|  _  ||  _  | |     /  ___|_   _/ _ \\ | ___ \\_   _|  ___|  _  \\
                | .  . |\\ V /    | | | | | || | | | |     \\ `--.  | |/ /_\\ \\| |_/ / | | | |__ | | | |
                | |\\/| | \\ /     | | | | | || | | | |      `--. \\ | ||  _  ||    /  | | |  __|| | | |
                | |  | | | |     | | \\ \\_/ /\\ \\_/ / |____ /\\__/ / | || | | || |\\ \\  | | | |___| |/ /\s
                \\_|  |_/ \\_/     \\_/  \\___/  \\___/\\_____/ \\____/  \\_/\\_| |_/\\_| \\_| \\_/ \\____/|___/ \s
                                                                                                    \s
                                                                                                    \s""");
	}

}
