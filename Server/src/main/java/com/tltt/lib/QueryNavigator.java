package com.tltt.lib;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tltt.lib.dto.QuidAnswerDTO;
import com.tltt.lib.dto.QuidQuestionDTO;
import com.tltt.lib.text.NoPredictionException;
import com.tltt.lib.text.OccurencesSearcher;

/**
 * NOTE TODO 
 * Bug quand les réponses sont des chiffres (le JS est aussi parsé et fausse les resultats) 
 * Faire une implem différente pour la derniére question chercher tout les chiffres de la page Parser le contenu de la premiere
 * page pour avoir de meilleurs résultats ? oui Parser si la question est une
 * question négative ?
 * Implementer la normalization des intutulés de question
 * Stopper d'utiliser les DTO dans la logique (remplacer par une class abstraite représentant une question)
 * Normalisation des élements de réponses enlevés les plueries( question sulky)
 * Ajouter un user agent d'ordinateur de bureau pour l'appel a la page google
 * Enlever les classes et id css qui polluent les réponses (cf question zorro)
 */
public class QueryNavigator {

	private static Logger logger = LogManager.getLogger();
	private String urlQuery;
	private OccurencesSearcher occurencesSearcher;
	private QuidQuestionDTO quidQuestionDTO;

	public QueryNavigator(QuidQuestionDTO quidQuestionDTO) throws UnsupportedEncodingException {
		this.quidQuestionDTO = quidQuestionDTO;
		this.urlQuery = new URLGenerator(quidQuestionDTO).getUrlQuery();
		String html = new HTMLBuilder(urlQuery).removeAccents(true).build();
		occurencesSearcher = new OccurencesSearcher(quidQuestionDTO, html);
	}

	public QuidAnswerDTO searchMostRelevantAnswer(QuidQuestionDTO quidQuestionDTO) throws NoPredictionException {
		QuidAnswerDTO quidAnswerDTO = occurencesSearcher.predictAnswer();
		return quidAnswerDTO;
	}

	public void openInBrowser() throws URISyntaxException, IOException {
		URI uri = new URI(urlQuery);
		java.awt.Desktop.getDesktop().browse(uri);
	}

	public void publishReport(DiscordWebhook discordWebhook) {
		ReportBuilder reportBuilder = new ReportBuilder().question(quidQuestionDTO).queryUrl(urlQuery).answersOccurences(occurencesSearcher.getAnswersOccurences());
		String strReport = reportBuilder.build();
		if (discordWebhook != null) {
			discordWebhook.addMessage(strReport);
			discordWebhook.send();
		}
		logger.debug(strReport);
	}

}
