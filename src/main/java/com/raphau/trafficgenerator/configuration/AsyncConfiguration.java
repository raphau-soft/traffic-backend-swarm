package com.raphau.trafficgenerator.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import org.springframework.web.client.RestTemplate;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.concurrent.Executor;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;


@Configuration
@EnableAsync
@EnableSwagger2
public class AsyncConfiguration {

    static final String topicExchangeNameBuyOffer = "buy-offer-exchange";
    static final String topicExchangeNameSellOffer = "sell-offer-exchange";
    static final String topicExchangeNameCompany = "company-exchange";
    static final String topicExchangeNameTestDetails = "test-details-exchange";
    static final String topicExchangeNameCpuData = "cpu-data-exchange";
    static final String topicExchangeNameUserData = "user-data-exchange";
    static final String topicExchangeNameStockData = "stock-data-exchange";
    static final String topicExchangeNameUserDataResponse = "user-data-response-exchange";
    static final String topicExchangeNameStockDataResponse = "stock-data-response-exchange";
    static final String topicExchangeNameRegisterRequest = "register-request-exchange";
    static final String topicExchangeNameRegisterResponse = "register-response-exchange";
    static final String topicExchangeNameTradeRequest = "trade-request-exchange";
    static final String topicExchangeNameTradeResponse = "trade-response-exchange";

    @Bean
    Queue queueRequestBuyOffer() {
        return new Queue("buy-offer-request", false);
    }

    @Bean
    Queue queueRequestSellOffer() {
        return new Queue("sell-offer-request", false);
    }

    @Bean
    Queue queueRequestCompany() {
        return new Queue("company-request", false);
    }

    @Bean
    Queue queueTestDetails() {
        return new Queue("test-details-response", false);
    }

    @Bean
    Queue queueCpuData() {
        return new Queue("cpu-data-request", false);
    }

    @Bean
    Queue queueUserData() {
        return new Queue("user-data-request", false);
    }

    @Bean
    Queue queueStockData() {
        return new Queue("stock-data-request", false);
    }

    @Bean
    Queue queueUserDataResponse() {
        return new Queue("user-data-response", false);
    }

    @Bean
    Queue queueStockDataResponse() {
        return new Queue("stock-data-response", false);
    }

    @Bean
    Queue queueRegisterRequest() {
        return new Queue("register-request", false);
    }

    @Bean
    Queue queueRegisterResponse() {
        return new Queue("register-response", false);
    }

    @Bean
    Queue queueTradeRequest() {
        return new Queue("trade-request", false);
    }

    @Bean
    Queue queueTradeResponse() {
        return new Queue("trade-response", false);
    }

    @Bean
    TopicExchange exchangeBuyOffer() {
        return new TopicExchange(topicExchangeNameBuyOffer);
    }

    @Bean
    TopicExchange exchangeSellOffer() {
        return new TopicExchange(topicExchangeNameSellOffer);
    }

    @Bean
    TopicExchange exchangeCompany() {
        return new TopicExchange(topicExchangeNameCompany);
    }

    @Bean
    TopicExchange exchangeTestDetails() {
        return new TopicExchange(topicExchangeNameTestDetails);
    }

    @Bean
    TopicExchange exchangeCpuData() {
        return new TopicExchange(topicExchangeNameCpuData);
    }

    @Bean
    TopicExchange exchangeUserData() {
        return new TopicExchange(topicExchangeNameUserData);
    }

    @Bean
    TopicExchange exchangeStockData() {
        return new TopicExchange(topicExchangeNameStockData);
    }

    @Bean
    TopicExchange exchangeUserDataResponse() {
        return new TopicExchange(topicExchangeNameUserDataResponse);
    }

    @Bean
    TopicExchange exchangeStockDataResponse() {
        return new TopicExchange(topicExchangeNameStockDataResponse);
    }

    @Bean
    TopicExchange exchangeRegisterRequest() {
        return new TopicExchange(topicExchangeNameRegisterRequest);
    }

    @Bean
    TopicExchange exchangeRegisterResponse() {
        return new TopicExchange(topicExchangeNameRegisterResponse);
    }

    @Bean
    TopicExchange exchangeTradeRequest() {
        return new TopicExchange(topicExchangeNameTradeRequest);
    }

    @Bean
    TopicExchange exchangeTradeResponse() {
        return new TopicExchange(topicExchangeNameTradeResponse);
    }

    @Bean
    Binding bindingBuyOffer(Queue queueRequestBuyOffer, TopicExchange exchangeBuyOffer) {
        return BindingBuilder.bind(queueRequestBuyOffer).to(exchangeBuyOffer).with("foo.bar.#");
    }

    @Bean
    Binding bindingSellOffer(Queue queueRequestSellOffer, TopicExchange exchangeSellOffer) {
        return BindingBuilder.bind(queueRequestSellOffer).to(exchangeSellOffer).with("foo.bar.#");
    }

    @Bean
    Binding bindingCompany(Queue queueRequestCompany, TopicExchange exchangeCompany) {
        return BindingBuilder.bind(queueRequestCompany).to(exchangeCompany).with("foo.bar.#");
    }

    @Bean
    Binding bindingTestDetails(Queue queueTestDetails, TopicExchange exchangeTestDetails) {
        return BindingBuilder.bind(queueTestDetails).to(exchangeTestDetails).with("foo.bar.#");
    }

    @Bean
    Binding bindingCpuData(Queue queueCpuData, TopicExchange exchangeCpuData) {
        return BindingBuilder.bind(queueCpuData).to(exchangeCpuData).with("foo.bar.#");
    }

    @Bean
    Binding bindingUserData(Queue queueUserData, TopicExchange exchangeUserData) {
        return BindingBuilder.bind(queueUserData).to(exchangeUserData).with("foo.bar.#");
    }

    @Bean
    Binding bindingStockData(Queue queueStockData, TopicExchange exchangeStockData) {
        return BindingBuilder.bind(queueStockData).to(exchangeStockData).with("foo.bar.#");
    }

    @Bean
    Binding bindingUserDataResponse(Queue queueUserDataResponse, TopicExchange exchangeUserDataResponse) {
        return BindingBuilder.bind(queueUserDataResponse).to(exchangeUserDataResponse).with("foo.bar.#");
    }

    @Bean
    Binding bindingStockDataResponse(Queue queueStockDataResponse, TopicExchange exchangeStockDataResponse) {
        return BindingBuilder.bind(queueStockDataResponse).to(exchangeStockDataResponse).with("foo.bar.#");
    }

    @Bean
    Binding bindingRegisterRequest(Queue queueRegisterRequest, TopicExchange exchangeRegisterRequest) {
        return BindingBuilder.bind(queueRegisterRequest).to(exchangeRegisterRequest).with("foo.bar.#");
    }

    @Bean
    Binding bindingRegisterResponse(Queue queueRegisterResponse, TopicExchange exchangeRegisterResponse) {
        return BindingBuilder.bind(queueRegisterResponse).to(exchangeRegisterResponse).with("foo.bar.#");
    }

    @Bean
    Binding bindingTradeRequest(Queue queueTradeRequest, TopicExchange exchangeTradeRequest) {
        return BindingBuilder.bind(queueTradeRequest).to(exchangeTradeRequest).with("foo.bar.#");
    }

    @Bean
    Binding bindingTradeResponse(Queue queueTradeResponse, TopicExchange exchangeTradeResponse) {
        return BindingBuilder.bind(queueTradeResponse).to(exchangeTradeResponse).with("foo.bar.#");
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }


    @Bean
    public AmqpTemplate template(ConnectionFactory co) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(co);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100000);
        executor.setMaxPoolSize(100000);
        executor.setQueueCapacity(200000);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory("rabbitmq");
        cachingConnectionFactory.setChannelCacheSize(1000000);
        return cachingConnectionFactory;
    }

//    @Bean
//    public ConnectionFactory connectionFactory() {
//        return new CachingConnectionFactory("localhost");
//    }

    @Bean
    public RabbitAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

}
