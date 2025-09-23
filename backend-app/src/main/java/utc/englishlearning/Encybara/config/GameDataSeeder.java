package utc.englishlearning.Encybara.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import utc.englishlearning.Encybara.domain.GameQuestion;
import utc.englishlearning.Encybara.domain.GameSession;
import utc.englishlearning.Encybara.domain.User;
import utc.englishlearning.Encybara.repository.GameSessionRepository;
import utc.englishlearning.Encybara.repository.UserRepository;
import utc.englishlearning.Encybara.util.constant.SpecialFieldEnum;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class GameDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GameSessionRepository gameSessionRepository;
    private final PasswordEncoder passwordEncoder;

    public GameDataSeeder(UserRepository userRepository,
                         GameSessionRepository gameSessionRepository,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🎮 Starting Game Data Seeding...");
        
        // Only seed if no game data exists
        if (gameSessionRepository.count() == 0) {
            seedUsers();
            seedCompletedGameSessions();
            System.out.println("✅ Game Data Seeding completed successfully!");
        } else {
            System.out.println("🔄 Game data already exists, skipping seeding");
        }
    }

    private void seedUsers() {
        System.out.println("👥 Seeding demo users...");
        
        List<User> demoUsers = Arrays.asList(
            createUser("Alice Johnson", "alice@example.com", "alice123", "0123456789", SpecialFieldEnum.IT, "Intermediate"),
            createUser("Bob Smith", "bob@example.com", "bob123", "0123456790", SpecialFieldEnum.ECONOMIC, "Beginner"),
            createUser("Carol Davis", "carol@example.com", "carol123", "0123456791", SpecialFieldEnum.EVERYONE, "Advanced"),
            createUser("David Wilson", "david@example.com", "david123", "0123456792", SpecialFieldEnum.CONSTRUCTION, "Intermediate"),
            createUser("Eve Brown", "eve@example.com", "eve123", "0123456793", SpecialFieldEnum.ELECTRICITY, "Beginner")
        );

        for (User user : demoUsers) {
            User existingUser = userRepository.findByEmail(user.getEmail());
            if (existingUser == null) {
                userRepository.save(user);
                System.out.println("   ✓ Created user: " + user.getEmail());
            }
        }
    }

    private User createUser(String name, String email, String password, String phone, SpecialFieldEnum field, String level) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password)); // Properly encode password
        user.setPhone(phone);
        user.setSpeciField(field);
        user.setEnglishlevel(level);
        return user;
    }

    private void seedCompletedGameSessions() {
        System.out.println("🎯 Seeding completed game sessions for leaderboard...");
        
        List<User> users = userRepository.findAll();
        
        if (users.isEmpty()) {
            System.out.println("⚠️ No users found, skipping game session seeding");
            return;
        }

        // Create completed game sessions with different scores
        for (int i = 0; i < Math.min(users.size(), 10); i++) {
            User user = users.get(i);
            int score = 50 + (i * 10) + (int)(Math.random() * 20); // Scores between 50-120
            
            GameSession session = new GameSession(user);
            session.setStartTime(LocalDateTime.now().minusHours(1 + i));
            session.setEndTime(LocalDateTime.now().minusHours(1 + i).plusSeconds(45));
            session.setScore(score);
            session.setCompleted(true);
            
            // Add sample questions to the session
            addSampleQuestionsToSession(session);
            
            gameSessionRepository.save(session);
            System.out.println("   ✓ Created completed session for user: " + user.getEmail() + " (Score: " + score + ")");
        }
    }

    private void addSampleQuestionsToSession(GameSession session) {
        List<String[]> sampleQuestions = Arrays.asList(
            new String[]{"What does 'abundant' mean?", "Plentiful", "Scarce", "Plentiful", "Expensive", "Difficult"},
            new String[]{"Choose the correct synonym for 'rapid':", "Fast", "Slow", "Fast", "Steady", "Careful"},
            new String[]{"What is the past tense of 'go'?", "Went", "Goed", "Went", "Gone", "Going"},
            new String[]{"Which word means 'very happy'?", "Ecstatic", "Sad", "Angry", "Ecstatic", "Tired"},
            new String[]{"What does 'procrastinate' mean?", "To delay", "To finish early", "To delay", "To organize", "To celebrate"}
        );

        for (String[] questionData : sampleQuestions) {
            GameQuestion question = new GameQuestion();
            question.setGameSession(session);
            question.setText(questionData[0]);
            question.setChoices(Arrays.asList(questionData[2], questionData[3], questionData[4], questionData[5]));
            question.setCorrectAnswer(questionData[1]);
            question.setCreatedAt(LocalDateTime.now());
            question.setAnswered(true);
            question.setUserAnswer(questionData[1]); // User answered correctly
            question.setAnsweredAt(LocalDateTime.now());
            
            session.getQuestions().add(question);
        }
    }
}