package br.ufmg.engsoft.reprova.tests.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import br.ufmg.engsoft.reprova.model.Questionnaire;
import br.ufmg.engsoft.reprova.database.Mongo;
import br.ufmg.engsoft.reprova.mocks.QuestionsDAOMock;
import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Question;
import br.ufmg.engsoft.reprova.tests.utils.EnvironmentUtils;


public class QuestionnaireTest {
  @BeforeEach
  public void init() throws Exception {
  	EnvironmentUtils.clearEnv();
  }
  
  /**
   * Questionnaire building without any optional values nor features.
   */
  @Test
  void buildSuccess_defaultValues() {
    var questionnaire = new Questionnaire.Builder()
      .build();
    
    assertFalse(questionnaire == null);
    assertEquals(null, questionnaire.id);
    assertEquals(0, questionnaire.questions.size());
    assertEquals(0, questionnaire.totalEstimatedTime);
    assertEquals(null, questionnaire.averageDifficulty);
  }
  
  /**
   * Questionnaire building with optional values and features.
   * @throws Exception 
   */
  @Test
  void buildSuccess_allValues() throws Exception {
  	EnvironmentUtils.setEnvVariables(true, 3);
  	var questions = new ArrayList<Question>();
  	var question = new Question.Builder()
        .theme("theme")
        .description("description")
  			.build();
  	
  	questions.add(question);
  	
    var questionnaire = new Questionnaire.Builder()
			.id("1")
    	.questions(questions)
    	.totalEstimatedTime(8)
    	.averageDifficulty("Average")
      .build();
    
    assertFalse(questionnaire == null);
    assertEquals("1", questionnaire.id);
    assertEquals(1, questionnaire.questions.size());
    assertEquals(8, questionnaire.totalEstimatedTime);
    assertEquals("Average", questionnaire.averageDifficulty);
  }
  
  /**
   * A question within the questionnaire mustn't be null.
   */
  @Test
  void nullQuestion() {
    var questions = new ArrayList<Question>();
    questions.add(null);
    
    assertThrows(
      IllegalArgumentException.class,
      () -> {
        new Questionnaire.Builder()
          .questions(questions)
          .build();
      }
    );
  }
  
  /**
   * Questionnaire generation without any optional values nor features.
   * @throws Exception 
   */
  @Test
  void generateSuccess_defaultValues() throws Exception {
  	var questionsDAO = new QuestionsDAOMock(8, 8);
  	
    var questionnaire = new Questionnaire.Generator()
      .generate(questionsDAO);
    
    assertFalse(questionnaire == null);
    assertEquals(null, questionnaire.id);
    assertEquals(5, questionnaire.questions.size());
    assertEquals(40, questionnaire.totalEstimatedTime);
    assertEquals("Average", questionnaire.averageDifficulty);
  }
  
  /**
   * Questionnaire generation with optional values and features.
   * @throws Exception 
   */
  @Test
  void generateSuccess_allValues() throws Exception {
  	EnvironmentUtils.setEnvVariables(true, 3);
  	var questionsDAO = new QuestionsDAOMock(8, 8);
  	
    var questionnaire = new Questionnaire.Generator()
			.id("1")
    	.totalEstimatedTime(8)
    	.averageDifficulty("Average")
    	.questionsCount(1)
      .generate(questionsDAO);
    
    assertFalse(questionnaire == null);
    assertEquals(null, questionnaire.id);
    assertEquals(1, questionnaire.questions.size());
    assertEquals(8, questionnaire.totalEstimatedTime);
    assertEquals("Average", questionnaire.averageDifficulty);
  }
  
  /**
   * Questionnaire generation with optional values and features.
   * @throws Exception 
   */
  @Test
  void generateSuccess_allValues_fiveDifficulties() throws Exception {
  	EnvironmentUtils.setEnvVariables(true, 5);
  	var questionsDAO = new QuestionsDAOMock(8, 8);
  	
    var questionnaire = new Questionnaire.Generator()
			.id("1")
    	.totalEstimatedTime(8)
    	.averageDifficulty("Average")
    	.questionsCount(1)
      .generate(questionsDAO);
    
    assertFalse(questionnaire == null);
    assertEquals(null, questionnaire.id);
    assertEquals(1, questionnaire.questions.size());
    assertEquals(8, questionnaire.totalEstimatedTime);
    assertEquals("Average", questionnaire.averageDifficulty);
  }
}
