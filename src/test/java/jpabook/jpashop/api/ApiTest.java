package jpabook.jpashop.api;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import jpabook.jpashop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static jpabook.jpashop.api.MemberApiController.*;
import static jpabook.jpashop.api.OrderApiController.*;
import static jpabook.jpashop.api.OrderApiController.NewOrderRequest.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.function.RequestPredicates.GET;

@SpringBootTest
@AutoConfigureMockMvc
class ApiTest {

    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private MockHttpSession session;
    @Autowired
    private MemberService memberService;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private EntityManager em;

    @BeforeEach
    void init(){
        Member admin = memberService.findOne(1L);
        session = new MockHttpSession();
        session.setAttribute("loginMember",admin);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .defaultRequest(get("/").session(session))
                .build();
    }

    @Test
    void memberJoin() throws Exception{
        JoinMemberRequest request = new JoinMemberRequest("testMember","1234","testCity","testStreet","testZipcode");
        checkWithRequestBody("POST","/api/join",request);
    }

    @Test
    void login() throws Exception {
        LoginRequestDto requestDto = new LoginRequestDto("userA","2222");

        checkWithRequestBody("POST","/api/login",requestDto);
    }

    @Test
    void newOrder() throws Exception {
        System.out.println("결과: "+itemService.findOne(1L).getName()+", "+itemService.findOne(1L).getStockQuantity());
        List<OrderItems> orderItems = new ArrayList<>();
        orderItems.add(new OrderItems("JPA1 BOOK","BOOK",10));
        NewOrderRequest request = new NewOrderRequest(new Address("city1","street1","zipcode1"),orderItems);
        checkWithRequestBody("POST","/api/order/new",request);

        System.out.println("결과: "+itemService.findOne(1L).getName()+", "+itemService.findOne(1L).getStockQuantity());
    }

    @Test
    void OrderValidate() throws Exception {
        Member member1 = memberService.findOne(2L);
        Member member2 = memberService.findOne(3L);

        MockHttpSession session1 = new MockHttpSession();
        session1.setAttribute("loginMember",member1);
        MockHttpSession session2 = new MockHttpSession();
        session2.setAttribute("loginMember",member2);

        List<OrderItems> orderItems = new ArrayList<>();
        orderItems.add(new OrderItems("JPA1 BOOK","BOOK",70));
        NewOrderRequest request = new NewOrderRequest(new Address("city1","street1","zipcode1"),orderItems);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);


        executorService.submit(()->{
                try {
            startLatch.await();
                    mockMvc.perform(post("/api/order/new")
                                    .session(session1)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.status").value("success"))
                            .andDo(print());
        } catch (Exception e) {
            e.printStackTrace();
        }
                finally {
                    endLatch.countDown();
                }
        });
        executorService.submit(()->{
            try {
                startLatch.await();
                mockMvc.perform(post("/api/order/new")
                                .session(session2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("success"))
                        .andDo(print());
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                endLatch.countDown();
            }
        });

        System.out.println("결과: "+itemService.findOne(1L).getName()+", "+itemService.findOne(1L).getStockQuantity());
        startLatch.countDown();
        endLatch.await();

        mockMvc.perform(get("/api/orders"))
                        .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                                .andDo(print());
        System.out.println("결과: "+itemService.findOne(1L).getName()+", "+itemService.findOne(1L).getStockQuantity());


        mockMvc.perform(get("/api/member-orders")
                        .session(session1))
                .andExpect(status().isOk())
                .andDo(print());


        mockMvc.perform(get("/api/member-orders")
                        .session(session2))
                .andExpect(status().isOk())
                .andDo(print());

    }





    private void checkWithRequestBody(String type,String url,Object request) throws Exception {
        switch (type){
            case "POST":
                mockMvc.perform(post(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("success"))
                        .andDo(print());
                break;
            case "GET":
                mockMvc.perform(get(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("success"))
                        .andDo(print());
                break;
            case "PUT":
                mockMvc.perform(put(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("success"))
                        .andDo(print());
                break;
            case "DELETE":
                mockMvc.perform(delete(url)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("success"))
                        .andDo(print());
                break;
        }
    }
}
