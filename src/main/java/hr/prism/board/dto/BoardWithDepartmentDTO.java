package hr.prism.board.dto;

public class BoardWithDepartmentDTO {

    private DepartmentDTO department;

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
