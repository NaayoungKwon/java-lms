package nextstep.qna.domain;

import java.util.List;
import java.util.stream.Collectors;
import nextstep.qna.CannotDeleteException;
import nextstep.users.domain.NsUser;

public class Answers {

  private final List<Answer> answers;
  public Answers(List<Answer> answers) {
    this.answers = answers;
  }

  public void add(Answer answer) {
    answers.add(answer);
  }

  public void validateDeletable(NsUser loginUser) throws CannotDeleteException {
    for (Answer answer : answers) {
      answer.validateDeletable(loginUser);
    }
  }

  public DeleteHistories delete() {
    return  new DeleteHistories(answers.stream()
                  .map(Answer::delete)
                  .collect(Collectors.toList()));
  }
}
