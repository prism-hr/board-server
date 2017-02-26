package hr.prism.board.dto;

import javax.validation.Valid;

public class BoardWithDepartmentDTO {

    @Valid
    private DepartmentDTO department;

    @Valid
    private BoardDTO board;

    public DepartmentDTO getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentDTO department) {
        this.department = department;
    }

    public BoardDTO getBoard() {
        return board;
    }

    public void setBoard(BoardDTO board) {
        this.board = board;
    }
}
