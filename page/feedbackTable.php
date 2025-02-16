<?php
libxml_use_internal_errors(true);

// XML load
$xml = simplexml_load_file("./outputfile.xml");

if ($xml === false) {
    echo "Error: Cannot create object\n";
    foreach (libxml_get_errors() as $error) {
        echo $error->message;
    }
    exit;
}

echo "<h3 style='background-color: #4CAF50; color: white;'>Alle Feedbacks</h3>";
echo "<table border='1' style='border-collapse: collapse; width: 100%;'>";
echo "<tr style='background-color: red; color: white; text-align: left;'>";
echo "<th><em>Salutation</em></th>";
echo "<th><em>First Name</em></th>";
echo "<th><em>Last Name</em></th>";
echo "<th><em>Age</em></th>";
echo "<th><em>Email Address</em></th>";
echo "<th><em>Telephone Number</em></th>";
echo "<th><em>Appearance Score</em></th>";
echo "<th><em>Content Score</em></th>";
echo "<th><em>Revisit</em></th>";
echo "<th><em>Suggestion</em></th>";
echo "<th><em>Date</em></th>";
echo "<th><em>Time</em></th>";
echo "</tr>";

$feedback_count = 0;

// XML read
foreach ($xml->feedback as $feedback) {
    // 'besucher' (visitor)
    $anrede = (string)$feedback->besucher['anrede'];  // Salutation (Professor, Doktor, etc.)
    $vorname = (string)$feedback->besucher['vorname'];  // First Name
    $nachname = (string)$feedback->besucher['nachname'];  // Last Name
    $alter = (string)$feedback->besucher->alter;  // Age
    $email = (string)$feedback->besucher->kontakt->emailadresse;  // Email Address
    $telefon = (string)$feedback->besucher->kontakt->telefonnummer;  // Telephone Number

    // 'bewertung' (rating)
    $note_aussehen = (string)$feedback->bewertung['note_aussehen'];  // Appearance Score
    $note_inhalt = (string)$feedback->bewertung['note_inhalt'];  // Content Score
    $erneuter_besuch = (string)$feedback->bewertung['erneuter_besuch'];  // Revisit
    $vorschlag = (string)$feedback->bewertung->vorschlag;  // Suggestion

    // 'info' (info) 
    $datum = (string)$feedback->info->datum;  // Date
    $uhrzeit = (string)$feedback->info->uhrzeit;  // Time
	
	if ($note_inhalt == "ungenuegend") {
        $note_inhalt = "ungenügend";
    }

    $aussehen_class = ($note_aussehen == "5" || $note_aussehen == "6") ? "style='color: red;'" : "";
    $inhalt_class = ($note_inhalt == "mangelhaft" || $note_inhalt == "ungenügend") ? "style='color: red;'" : "";

    // table
    echo "<tr>";
    echo "<td>$anrede</td>";
    echo "<td>$vorname</td>";
    echo "<td>$nachname</td>";
    echo "<td>$alter</td>";
    echo "<td>$email</td>";
    echo "<td>$telefon</td>";
    echo "<td $aussehen_class>$note_aussehen</td>";
    echo "<td $inhalt_class>$note_inhalt</td>";
    echo "<td>$erneuter_besuch</td>";
    echo "<td>$vorschlag</td>";
    echo "<td>$datum</td>";
    echo "<td>$uhrzeit</td>";
    echo "</tr>";

    $feedback_count++;
}

echo "</table>";

echo "<p style='font-style: italic; color: gray;'>The number of participants: $feedback_count</p>";

echo "<p style='font-style: italic; color: gray;'>" . $xml->entwickler_parser . "</p>";
?>
