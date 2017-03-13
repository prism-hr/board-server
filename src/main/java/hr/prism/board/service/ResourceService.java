package hr.prism.board.service;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceRelation;
import hr.prism.board.repository.ResourceRelationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service
@Transactional
public class ResourceService {
    
    @Inject
    private ResourceRelationRepository resourceRelationRepository;
    
    public void createResourceRelation(Resource resource1, Resource resource2) {
        if (resource1.getType().equals("BOARD") && resource2.getType().equals("DEPARTMENT")) {
            throw new IllegalStateException("A board cannot be the parent of a department");
        }
        
        ResourceRelation resourceRelation = new ResourceRelation().setResource1(resource1).setResource2(resource2);
        resourceRelationRepository.save(resourceRelation);
        
        resource1.getChildren().add(resourceRelation);
        resource2.getParents().add(resourceRelation);
    }
    
}
