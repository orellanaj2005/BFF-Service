package com.smartlogix.bff;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "bff.services.usuario-url=http://localhost:9001",
    "bff.services.inventario-url=http://localhost:9002"
})
class BffApplicationTests {

    @Test
    void contextLoads() {
    }
}
