package nextstep.courses.infrastructure;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import nextstep.courses.domain.*;
import nextstep.courses.domain.Image.Dimension;
import nextstep.users.domain.NsUser;
import nextstep.users.domain.UserAuthorization;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository("sessionRepository")
public class JdbcSessionRepository implements SessionRepository {

    private JdbcOperations jdbcTemplate;

    public JdbcSessionRepository(JdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Session save(Session session) {
        List<Long> imageIds = insertImages(session.getImages());
        Long sessionId = insertSession(session);
        insertImageSession(sessionId, imageIds);
        session.setId(sessionId);
        return session;
    }

    private void insertImageSession(Long sessionId, List<Long> imageIds) {
        String sql = "insert into session_image (session_id, image_id, created_at) values(?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, sessionId);
                ps.setLong(2, imageIds.get(i));
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            }

            @Override
            public int getBatchSize() {
                return imageIds.size();
            }
        });
    }

    private List<Long> insertImages(Images images) {
        String imageSql = "insert into image (size, type, width, height, created_at) values(?, ?, ?, ?, ?)";
        List<Long> generatedIds = new ArrayList<>();
        for (Image image : images.getImages()) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(imageSql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, image.getSize());
                ps.setString(2, image.getType());
                ps.setInt(3, image.getWidth());
                ps.setInt(4, image.getHeight());
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                return ps;
            }, keyHolder);

            generatedIds.add(keyHolder.getKey().longValue());
        }

        return generatedIds;
    }

    private Long insertSession(Session session) {
        String sql = "insert into session (course_id, start_date, end_date, pay_type, session_state, recruitment_state, fee, created_at) values(?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, session.getCourseId());
            ps.setTimestamp(2, Timestamp.valueOf(session.getStartDate().atStartOfDay()));
            ps.setTimestamp(3, Timestamp.valueOf(session.getEndDate().atStartOfDay()));
            ps.setString(4, session.getPayType());
            ps.setString(5, session.getSessionState());
            ps.setString(6, session.getRecruitmentState());
            ps.setLong(7, session.getFee());
            ps.setTimestamp(8, Timestamp.valueOf(session.getCreatedAt()));
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Session findById(Long id) {
        Images images = findImagesBySessionId(id);

        String sql = "select s.id, s.course_id, s.start_date, s.end_date, s.pay_type, s.session_state, s.recruitment_state, s.max_student, s.fee, c.title, c.creator_id "
            + "from session s "
            + "join course c on s.course_id = c.id "
            + "where s.id = ?";
        RowMapper<Session> rowMapper = (rs, rowNum) ->
            new Session(
                rs.getLong(1),
                new Course(rs.getLong(2), rs.getString(10), rs.getLong(11)),
                new SessionDuration(toLocalDate(rs.getTimestamp(3)), toLocalDate(rs.getTimestamp(4))),
                images,
                SessionPayType.valueOf(rs.getString(5)),
                SessionState.valueOf(rs.getString(6)),
                RecruitmentState.valueOf(rs.getString(7)),
                rs.getInt(8),
                rs.getLong(9),
                new SessionStudent( rs.getInt(8))
            );

        return jdbcTemplate.queryForObject(sql, rowMapper, id);
    }

    private Images findImagesBySessionId(Long sessionId){
        String sql = "select i.id, i.size, i.type, i.width, i.height from image i join session_image si on i.id = si.image_id where si.session_id = ?";
        RowMapper<Image> rowMapper = (rs, rowNum) -> new Image(rs.getLong(1), rs.getInt(2), ImageType.valueOf(rs.getString(3)), new Dimension(rs.getInt(4), rs.getInt(5)));
        List<Image> images = jdbcTemplate.query(sql, rowMapper, sessionId);
        return new Images(images);
    }

    @Override
    public void saveStudents(Session session) {
        String sql = "insert into study_student (session_id, student_id, created_at) values(?, ?, ?)";
        session.getStudentsId().forEach(studentId -> jdbcTemplate.update(sql, session.getId(), studentId, Timestamp.valueOf(LocalDateTime.now())));
    }

    @Override
    public void updateStudentSelect(Long sessionId, SelectedStudents students) {
        String sql = "update study_student set selection_status = ? where session_id = ? and student_id = ?";
        students.getAcceptedStudents().forEach(student -> jdbcTemplate.update(sql, "ACCEPTED", sessionId, student.getId()));
        students.getRejectedStudents().forEach(student -> jdbcTemplate.update(sql, "REJECTED", sessionId, student.getId()));
    }

    @Override
    public SessionStudent findAcceptedStudentsById(Long sessionId) {
        String sql = "select u.id, u.user_id, u.name, u.email, u.`authorization` from study_student as s join ns_user as u on s.student_id = u.id where s.session_id = ? and s.selection_status =  'ACCEPTED'";
        RowMapper<NsUser> rowMapper = (rs, rowNum) -> new NsUser(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4),
            UserAuthorization.valueOf(rs.getString(5)));
       return new SessionStudent(jdbcTemplate.query(sql, rowMapper, sessionId));
    }

    private LocalDate toLocalDate(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime().toLocalDate();
    }
}
