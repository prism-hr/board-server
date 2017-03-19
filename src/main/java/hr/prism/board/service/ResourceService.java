package hr.prism.board.service;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceRelation;
import hr.prism.board.domain.Scope;
import hr.prism.board.repository.ResourceRelationRepository;
import hr.prism.board.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

@Service
@Transactional
public class ResourceService {
    
    @Inject
    private ResourceRepository resourceRepository;
    
    @Inject
    private ResourceRelationRepository resourceRelationRepository;
    
    public Resource findOne(Long id) {
        return resourceRepository.findOne(id);
    }
    
    public Resource findByHandle(String handle) {
        return resourceRepository.findByHandle(handle);
    }
    
    public void updateHandle(Resource resource, String newHandle) {
        resource.setHandle(newHandle);
        resourceRepository.updateHandle(resource.getHandle(), newHandle);
    }
    
    public void createResourceRelation(Resource resource1, Resource resource2) {
        if (resource1.getScope() == Scope.BOARD && resource2.getScope() == Scope.DEPARTMENT) {
            throw new IllegalStateException("A board cannot be the parent of a department");
        }
        
        ResourceRelation resourceRelation = new ResourceRelation().setResource1(resource1).setResource2(resource2);
        resourceRelationRepository.save(resourceRelation);
        
        resource1.getChildren().add(resourceRelation);
        resource2.getParents().add(resourceRelation);
    }
    
}
