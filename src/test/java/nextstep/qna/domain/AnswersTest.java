package nextstep.qna.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import nextstep.users.domain.NsUserTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AnswersTest {

  private static final Question Q1 = new Question(NsUserTest.JAVAJIGI, "title1", "contents1");
  private static final Question Q2 = new Question(NsUserTest.SANJIGI, "title2", "contents2");
  @Test
  @DisplayName("Answers 삭제하면 DeleteHistory를 반환한다")
  void delete(){
    Answer answer = new Answer(NsUserTest.JAVAJIGI, Q1, "Answers Contents");
    Answers answers = new Answers(List.of(answer));

    List<DeleteHistory> deleteHistories = answers.delete().getDeleteHistories();

    assertThat(deleteHistories).hasSize(1);
    assertThat(deleteHistories).containsExactly(new DeleteHistory(ContentType.ANSWER, answer.getId(), answer.getWriter(),
        LocalDateTime.now()));
  }

  @Test
  @DisplayName("Answers 삭제하면 answer는 delete 상태가 된다.")
  void checkDeleteState(){
    Answer answer = new Answer(NsUserTest.SANJIGI, Q2, "Answers Contents");
    Answers answers = new Answers(List.of(answer));

    answers.delete();

    assertThat(answer.isDeleted()).isTrue();
  }

}
