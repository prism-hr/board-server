package hr.prism.board.service;

import hr.prism.board.domain.Board;
import hr.prism.board.domain.Department;
import hr.prism.board.dto.BoardDTO;
import hr.prism.board.dto.BoardSettingsDTO;
import hr.prism.board.mapper.BoardMapper;
import hr.prism.board.mapper.DepartmentMapper;
import hr.prism.board.repository.BoardRepository;
import hr.prism.board.representation.DepartmentRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class BoardService {

    @Inject
    private DepartmentService departmentService;

    @Inject
    private UserService userService;

    @Inject
    private BoardRepository boardRepository;

    @Inject
    private BoardMapper boardMapper;

    @Inject
    private DepartmentMapper departmentMapper;

    public Iterable<Board> getBoards() {
        return boardRepository.findAll();
    }

    public List<DepartmentRepresentation> getBoardsGroupedByDepartment() {
        Iterable<Board> boards = getBoards();
        Map<Long, DepartmentRepresentation> departmentsMap = new HashMap<>();
        for (Board board : boards) {
            Department department = board.getDepartment();
            Long departmentId = department.getId();
            DepartmentRepresentation departmentRepresentation;
            if (departmentsMap.containsKey(departmentId)) {
                departmentRepresentation = departmentsMap.get(departmentId);
            } else {
                departmentRepresentation = departmentMapper.apply(department);
                departmentRepresentation.setBoards(new LinkedList<>());
                departmentsMap.put(departmentId, departmentRepresentation);
            }
            departmentRepresentation.getBoards().add(boardMapper.apply(board));
        }
        return departmentsMap.entrySet()
            .stream()
            .map(Map.Entry::getValue)
            .sorted(Comparator.comparing(DepartmentRepresentation::getName))
            .collect(Collectors.toList());
    }

    public Board getBoard(Long id) {
        return boardRepository.findOne(id);
    }

    public Board createBoard(BoardDTO boardDTO) {
        Department department = departmentService.getOrCreateDepartment(boardDTO.getDepartment());
        Board board = new Board();
        board.setName(boardDTO.getName());
        board.setPurpose(boardDTO.getPurpose());
        board.setPostCategories("");
        board.setDepartment(department);
        board.setUser(userService.getCurrentUser());
        return boardRepository.save(board);
    }

    public void updateBoard(BoardDTO boardDTO) {
        Board board = boardRepository.findOne(boardDTO.getId());
        board.setName(boardDTO.getName());
        board.setPurpose(boardDTO.getPurpose());
    }

    public void updateBoardSettings(Long id, BoardSettingsDTO boardSettingsDTO) {
        Board board = boardRepository.findOne(id);
        board.setPostCategories(boardSettingsDTO.getPostCategories().stream().collect(Collectors.joining("|")));
    }
}
