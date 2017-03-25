package hr.prism.board.authentication;

import hr.prism.board.domain.Role;
import hr.prism.board.domain.Scope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockApi {
    
    @Restriction(scope = Scope.BOARD, roles = Role.ADMINISTRATOR)
    public void getBoards() {
    }
    
    @Restriction(scope = Scope.DEPARTMENT, roles = Role.ADMINISTRATOR)
    public void getDepartments() {
    }
    
    @Restriction(scope = Scope.BOARD, roles = Role.ADMINISTRATOR)
    public void getBoard(@PathVariable("id") Long id) {
    }
    
    @Restriction(scope = Scope.DEPARTMENT, roles = Role.ADMINISTRATOR)
    public void getDepartment(@PathVariable("id") Long id) {
    }
    
    @Restriction(scope = Scope.BOARD, roles = Role.ADMINISTRATOR)
    public void getBoard(@PathVariable("handle") String handle) {
    }
    
    @Restriction(scope = Scope.DEPARTMENT, roles = Role.ADMINISTRATOR)
    public void getDepartment(@PathVariable("handle") String handle) {
    }
    
    @Restriction(scope = Scope.DEPARTMENT, roles = Role.ADMINISTRATOR)
    public void invalidArgument(@PathVariable String handle) {
    }
    
    @Restriction(scope = Scope.DEPARTMENT, roles = Role.ADMINISTRATOR)
    public void invalidSignature(String handle) {
    }
    
}
