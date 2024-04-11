package server.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@org.springframework.stereotype.Controller
public class Controller {

    /**
     * Respond with status 204 to let the client know
     * if the server is up or their URL is correct
     *
     * @return 204 status
     */
    @GetMapping("ping")
    public ResponseEntity<String> ping() {
        try {
            return ResponseEntity.noContent().build(); // pong
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // ??
        }
    }
}
