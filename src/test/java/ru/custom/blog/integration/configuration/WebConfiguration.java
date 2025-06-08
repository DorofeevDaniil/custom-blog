package ru.custom.blog.integration.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@Profile("integration")
@ComponentScan(basePackages = {"ru.custom.blog"})
public class WebConfiguration {
}