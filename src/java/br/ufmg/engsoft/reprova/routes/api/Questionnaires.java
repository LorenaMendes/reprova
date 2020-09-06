package br.ufmg.engsoft.reprova.routes.api;

import spark.Spark;
import spark.Request;
import spark.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.engsoft.reprova.database.QuestionnairesDAO;
import br.ufmg.engsoft.reprova.database.QuestionsDAO;
import br.ufmg.engsoft.reprova.model.Question;
import br.ufmg.engsoft.reprova.model.Questionnaire;
import br.ufmg.engsoft.reprova.mime.json.Json;

/**
 * Questionnaires route.
 */
public class Questionnaires {
  /**
   * Logger instance.
   */
  protected static final Logger logger = LoggerFactory.getLogger(Questionnaires.class);

  /**
   * Access token.
   */
  protected static final String token = System.getenv("REPROVA_TOKEN");

  /**
   * Messages.
   */
  protected static final String unauthorized = "\"Unauthorized\"";
  protected static final String invalid = "\"Invalid request\"";
  protected static final String ok = "\"Ok\"";

  /**
   * Json formatter.
   */
  protected final Json json;
  /**
   * DAO for Questionnaire.
   */
  protected final QuestionnairesDAO questionnairesDAO;
  /**
   * DAO for Question.
   */
  protected final QuestionsDAO questionsDAO;

  /**
   * Instantiate the questionnaires endpoint.
   * The setup method must be called to install the endpoint.
   * @param json          the json formatter
   * @param questionnairesDAO  the DAO for Questionnaire
   * @param questionsDAO  the DAO for Question
   * @throws IllegalArgumentException  if any parameter is null
   */
  public Questionnaires(Json json, QuestionnairesDAO questionnairesDAO, QuestionsDAO questionsDAO) {
    if (json == null) {
      throw new IllegalArgumentException("json mustn't be null");
    }

    if (questionnairesDAO == null) {
      throw new IllegalArgumentException("questionnairesDAO mustn't be null");
    }

    if (questionsDAO == null) {
      throw new IllegalArgumentException("questionsDAO mustn't be null");
    }

    this.json = json;
    this.questionnairesDAO = questionnairesDAO;
    this.questionsDAO = questionsDAO;
  }


  /**
   * Install the endpoint in Spark.
   * Methods:
   * - get
   * - post
   * - delete
   */
  public void setup() {
    Spark.get("/api/questionnaires", this::get);
    Spark.post("/api/questionnaires", this::post);
    Spark.delete("/api/questionnaires", this::delete);

    logger.info("Setup /api/questionnaires.");
  }

  /**
   * Check if the given token is authorized.
   */
  protected static boolean authorized(String token) {
    return Questionnaires.token.equals(token);
  }

  /**
   * Get endpoint: lists all questionnaires, or a single questionnaire if a 'id' query parameter is
   * provided.
   */
  protected Object get(Request request, Response response) {
    logger.info("Received questionnaires get:");

    var id = request.queryParams("id");
    var auth = authorized(request.queryParams("token"));
      
    if (id == null) {
    	return this.get(request, response, auth);
    }
     
    return this.get(request, response, id, auth);
  }

  /**
   * Get id endpoint: fetch the specified questionnaire from the database.
   * If not authorised, and the given questionnaire is private, returns an error message.
   */
  protected Object get(Request request, Response response, String id, boolean auth) {
    if (id == null) {
      throw new IllegalArgumentException("id mustn't be null");
    }

    response.type("application/json");

    logger.info("Fetching questionnaire " + id);

    var questionnaire = questionnairesDAO.get(id);

    if (questionnaire == null) {
      logger.error("Invalid request!");
      response.status(400);
      return invalid;
    }

    logger.info("Done. Responding...");

    response.status(200);

    return json.render(questionnaire);
  }

  /**
   * Get all endpoint: fetch all questionnaires from the database.
   * If not authorized, fetches only public questionnaires.
   */
  protected Object get(Request request, Response response, boolean auth) {
    response.type("application/json");

    logger.info("Fetching questionnaires.");

    var questionnaires = questionnairesDAO.list();

    logger.info("Done. Responding...");

    response.status(200);

    return json.render(questionnaires);
  }


  /**
   * Post endpoint: add or update a questionnaire in the database.
   * The questionnaire must be supplied in the request's body.
   * If the questionnaire has an 'id' field, the operation is an update.
   * Otherwise, the given questionnaire is added as a new questionnaire in the database.
   * This endpoint is for authorized access only.
   */
  protected Object post(Request request, Response response) {
    String body = request.body();

    logger.info("Received questionnaires post:" + body);

    response.type("application/json");

    var token = request.queryParams("token");

    if (!authorized(token)) {
      logger.info("Unauthorized token: " + token);
      response.status(403);
      return unauthorized;
    }

    Questionnaire questionnaire;
    try {
      questionnaire = json
        .parse(body, Questionnaire.Builder.class)
        .build();
    }
    catch (Exception e) {
      logger.error("Invalid request payload!", e);
      response.status(400);
      return invalid;
    }

    logger.info("Parsed " + questionnaire.toString());
    logger.info("Adding questionnaire.");

    var success = questionnairesDAO.add(questionnaire);

    logger.info("Added questionnaire.");
    logger.info("Adding questions.");

    for (var question : questionnaire.questions){
      question = new Question.Builder()
                     .theme(question.theme)
                     .description(question.description)
                     .statement(question.statement)
                     .pvt(question.pvt)
                     .build();
                     
      questionsDAO.add(question);
    }

    response.status(
       success ? 200
               : 400
    );

    logger.info("Done. Responding...");

    return ok;
  }


  /**
   * Delete endpoint: remove a questionnaire from the database.
   * The questionnaire's id must be supplied through the 'id' query parameter.
   * This endpoint is for authorized access only.
   */
  protected Object delete(Request request, Response response) {
    logger.info("Received questionnaires delete:");

    response.type("application/json");

    var id = request.queryParams("id");
    var token = request.queryParams("token");

    if (!authorized(token)) {
      logger.info("Unauthorized token: " + token);
      response.status(403);
      return unauthorized;
    }

    if (id == null) {
      logger.error("Invalid request!");
      response.status(400);
      return invalid;
    }

    logger.info("Deleting questionnaire " + id);

    var success = questionnairesDAO.remove(id);

    logger.info("Done. Responding...");

    response.status(
      success ? 200
              : 400
    );

    return ok;
  }
}