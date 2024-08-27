import java.util.*;
import java.util.concurrent.*;

class Question {
    private String question;
    private List<String> options;
    private char correctAnswer;

    // Constructor to initialize a question
    public Question(String question, List<String> options, char correctAnswer) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    // Getters
    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public char getCorrectAnswer() {
        return correctAnswer;
    }
}

class UserAnswer {
    String questionText;
    char correctAnswer;
    char userAnswer;
    boolean isCorrect;

    // Constructor to initialize the user's answer details
    public UserAnswer(String questionText, char correctAnswer, char userAnswer, boolean isCorrect) {
        this.questionText = questionText;
        this.correctAnswer = correctAnswer;
        this.userAnswer = userAnswer;
        this.isCorrect = isCorrect;
    }
}

class Quiz {
    private List<Question> questions;
    private List<UserAnswer> userAnswers;
    private int score;
    private String playerName;

    // Constructor to initialize the quiz with questions and player name
    public Quiz(List<Question> questions, String playerName) {
        this.questions = questions;
        this.userAnswers = new ArrayList<>();
        this.score = 0;
        this.playerName = playerName;
    }

    // Method to start the quiz
    public void start() {
        Scanner scanner = new Scanner(System.in);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        int questionNumber = 1;

        System.out.println("\nWelcome, " + playerName + "! Get ready to test your knowledge!");

        for (Question question : questions) {
            System.out.println("\nQuestion " + questionNumber + ": " + question.getQuestion());

            List<String> options = question.getOptions();
            for (int i = 0; i < options.size(); i++) {
                System.out.println((char) ('A' + i) + ": " + options.get(i));
            }

            System.out.println("You have 15 seconds to answer. Please enter your choice (A, B, C, D):");

            Future<String> futureAnswer = executor.submit(() -> {
                String answer = scanner.nextLine().trim().toUpperCase(); // Convert input to uppercase
                return answer;
            });

            try {
                // Wait for user input or timeout
                String userAnswer = futureAnswer.get(15, TimeUnit.SECONDS);

                // Check if the answer is correct
                boolean isCorrect = userAnswer.length() > 0 && userAnswer.charAt(0) == question.getCorrectAnswer();
                if (isCorrect) {
                    System.out.println("Correct!");
                    score++;
                } else {
                    System.out.println("Incorrect! The correct answer was " + question.getCorrectAnswer());
                }

                // Store user answer
                userAnswers.add(new UserAnswer(question.getQuestion(), question.getCorrectAnswer(),
                        userAnswer.isEmpty() ? ' ' : userAnswer.charAt(0), isCorrect));

            } catch (TimeoutException e) {
                // Timeout occurred
                System.out.println("Time's up! The correct answer was " + question.getCorrectAnswer());
                futureAnswer.cancel(true);

                // Store the timeout result
                userAnswers.add(new UserAnswer(question.getQuestion(), question.getCorrectAnswer(), ' ', false));
            } catch (Exception e) {
                System.out.println("An error occurred while reading the answer.");
            }

            questionNumber++;
        }

        executor.shutdown();
        showResults();
    }

    // Method to display the quiz results in a tabular format
    private void showResults() {
        System.out.println("\nQuiz Completed!");
        System.out.println("+--------------------------------------------------+---------------+----------------+--------------+");
        System.out.println("|                   Question                      | User Answer   | Correct Answer | Result       |");
        System.out.println("+--------------------------------------------------+---------------+----------------+--------------+");

        for (UserAnswer answer : userAnswers) {
            System.out.printf("| %-48s | %-13s | %-14s | %-12s |\n",
                    formatString(answer.questionText, 48),
                    answer.userAnswer == ' ' ? "Skipped" : String.valueOf(answer.userAnswer),
                    String.valueOf(answer.correctAnswer),
                    answer.isCorrect ? "Correct" : "Incorrect");
        }

        System.out.println("+--------------------------------------------------+---------------+----------------+--------------+");
        System.out.printf("Your final score: %d/%d\n", score, questions.size());
        System.out.println("Thank you for playing, " + playerName + "! We hope you enjoyed the quiz.");
    }

    // Helper method to format strings to fit column width
    private String formatString(String str, int width) {
        if (str.length() > width) {
            return str.substring(0, width - 3) + "..."; // Truncate and add ellipsis if too long
        } else {
            return String.format("%-" + width + "s", str); // Pad with spaces if too short
        }
    }
}

public class QuizApplication {
    public static void main(String[] args) {
        // Initialize questions
        List<Question> questions = new ArrayList<>();
        questions.add(new Question("What is the capital of France?", Arrays.asList("Berlin", "Madrid", "Paris", "Lisbon"), 'C'));
        questions.add(new Question("What is 2 + 2?", Arrays.asList("3", "4", "5", "6"), 'B'));
        questions.add(new Question("Which planet is known as the Red Planet?", Arrays.asList("Earth", "Mars", "Jupiter", "Saturn"), 'B'));

        // Get player's name
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your name to start the quiz: ");
        String playerName = scanner.nextLine().trim();

        // Initialize and start the quiz
        Quiz quiz = new Quiz(questions, playerName);
        quiz.start();
    }
}
