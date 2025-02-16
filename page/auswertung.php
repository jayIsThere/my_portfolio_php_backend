<?php
if ($_SERVER["REQUEST_METHOD"] === "POST") {

    ob_start();


    $currentDate = date("Y-m-d");
    $currentTime = date("H:i");

	$salutation = isset($_POST['anrede']) ? htmlspecialchars($_POST['anrede']) : null;	
    $firstName = isset($_POST['vorname']) ? htmlspecialchars($_POST['vorname']) : 'Unknown';
    $lastName = isset($_POST['nachname']) ? htmlspecialchars($_POST['nachname']) : 'Unknown';
    $email = isset($_POST['email']) ? htmlspecialchars($_POST['email']) : null;
    $phone = isset($_POST['telefonnummer']) ? htmlspecialchars($_POST['telefonnummer']) : null;
	$website = isset($_POST['website']) ? htmlspecialchars($_POST['website']) : null;
    $age = isset($_POST['age']) ? htmlspecialchars($_POST['age']) : 'Unknown';
    $repeatVisit = isset($_POST['besuch']) ? 'ja' : 'nein';
    $suggestion = isset($_POST['verbesserung']) ? htmlspecialchars($_POST['verbesserung']) : null;
    $contentRating = isset($_POST['note_inhalt']) ? htmlspecialchars($_POST['note_inhalt']) : null;
	
	
	if ($contentRating === "1") {
	$contentRating = "sehr_gut";
	}
	if ($contentRating === "2") {
	$contentRating = "gut";
	}
	if ($contentRating === "3") {
	$contentRating = "befriedigend";
	}
	if ($contentRating === "4") {
	$contentRating = "ausreichend";
	}
	if ($contentRating === "5") {
	$contentRating = "mangelhaft";
	}
	if ($contentRating === "6") {
	$contentRating = "ungenuegend";
	}
    $appearanceRating = isset($_POST['note_aussehen']) ? htmlspecialchars($_POST['note_aussehen']) : null;
    $password = isset($_POST['passwort']) ? htmlspecialchars($_POST['passwort']) : 'Unknown';

    if ($password !== 'Internetsprachen') {
        ob_end_clean(); // Error message before creating a xml file
        echo "<p>Error: Spam protection question was not answered correctly.</p>";
        exit;
    }

    // XML file create
    $fileName = "feedback_" . date("Ymd_His") . ".xml";
    $xml = new XMLWriter();
    if (!$xml->openURI($fileName)) {
        ob_end_clean();
        echo "<p>Error: XML file could not be created.</p>";
        exit;
    }

    $xml->setIndent(true);
    $xml->setIndentString("    ");
    $xml->startDocument('1.0', 'UTF-8');
	
    // DOCTYPE
    $xml->writeRaw('<!DOCTYPE feedbackdatenbank SYSTEM "feedbackdatenbank.dtd">');

    // root
	$xml->startElement('feedbackdatenbank');
	
    $xml->startElement('feedback');

    // visitor 
    $xml->startElement('besucher');
    $xml->writeAttribute('anrede', $salutation);
    $xml->writeAttribute('vorname', $firstName);
    $xml->writeAttribute('nachname', $lastName);

    $xml->startElement('alter');
    $xml->text($age);
    $xml->endElement();

	// contact
    $xml->startElement('kontakt');
    if ($email) {
        $xml->startElement('emailadresse');
        $xml->text($email);
        $xml->endElement();
    }
    if ($website) {
        $xml->startElement('website');
        $xml->text($website);
        $xml->endElement();
    }
    if ($phone) {
        $xml->startElement('telefonnummer');
        $xml->text($phone);
        $xml->endElement();
    }
    $xml->endElement(); // contact end

    $xml->endElement(); // visitor end

    // rating 
    $xml->startElement('bewertung');
    $xml->writeAttribute('erneuter_besuch', $repeatVisit);
    if ($contentRating) {
        $xml->writeAttribute('note_inhalt', $contentRating);
    }
    if ($appearanceRating) {
        $xml->writeAttribute('note_aussehen', $appearanceRating);
    }
    if ($suggestion) {
        $xml->startElement('vorschlag');
        $xml->text($suggestion);
        $xml->endElement();
    }
    $xml->endElement(); // rating end

    // info 
    $xml->startElement('info');
    $xml->startElement('datum');
    $xml->text($currentDate);
    $xml->endElement();

    $xml->startElement('uhrzeit');
    $xml->text($currentTime);
    $xml->endElement();

    $xml->endElement(); // info end

    $xml->endElement(); // feedback end
	
	$xml->startElement('entwickler_parser');
	$xml->text('Developed by Jaehan Kim');
	$xml->endElement();

	$xml->endElement(); // feedbackdatenbank end
    $xml->endDocument();
    $xml->flush();
	

    ob_end_clean(); // XML create 
    echo "<p>Your feedback has been successfully saved.</p>";
    echo "<p>The file has been saved as <strong>$fileName</strong>.</p>";
	echo "<p>Current date and time is $currentDate $currentTime.</p>";
}
?>
