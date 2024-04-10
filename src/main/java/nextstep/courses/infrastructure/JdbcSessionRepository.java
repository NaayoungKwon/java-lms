package nextstep.courses.infrastructure;

import nextstep.courses.domain.Course;
import nextstep.courses.domain.Image;
import nextstep.courses.domain.Session;
import nextstep.courses.domain.SessionRepository;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
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
        // save image
        Image image = session.getImage();
        String sql = "insert into session (course_id, image_id, start_date, end_Date, pay_type, state, fee, created_at) values(?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, session.getCourseId());
            ps.setLong(2, image.getId());
            ps.setTimestamp(3, Timestamp.valueOf(session.getStartDate().atStartOfDay()));
            ps.setTimestamp(4, Timestamp.valueOf(session.getEndDate().atStartOfDay()));
            ps.setString(5, session.getPayType());
            ps.setString(6, session.getState());
            ps.setLong(7, session.getFee());
            ps.setTimestamp(8, Timestamp.valueOf(session.getCreatedAt()));
            return ps;
        }, keyHolder);
        long key = keyHolder.getKey().longValue();
        session.setId(key);
        return session;
    }

    @Override
    public Session findById(Long id) {
        String sql = "select id, pay_type  from course where id = ?";
        RowMapper<Session> rowMapper = (rs, rowNum) -> new Session(
                rs.getLong(1),
                rs.getString(2);
//                rs.getLong(3),
//                toLocalDateTime(rs.getTimestamp(4)),
//                toLocalDateTime(rs.getTimestamp(5)));
        return jdbcTemplate.queryForObject(sql, rowMapper, id);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }

    private LocalDate toLocalDate(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime().toLocalDate();
    }
}
