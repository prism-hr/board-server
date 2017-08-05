package hr.prism.board.repository;

import hr.prism.board.domain.Resource;
import hr.prism.board.domain.ResourceEvent;
import hr.prism.board.domain.User;

public interface ResourceEventRepository extends MyRepository<ResourceEvent, Long> {

    ResourceEvent findFirstByResourceAndEventAndUserOrderByIdDesc(Resource resource, hr.prism.board.enums.ResourceEvent event, User user);

    ResourceEvent findFirstByResourceAndEventAndIpAddressOrderByIdDesc(Resource resource, hr.prism.board.enums.ResourceEvent event, String ipAddress);


}
