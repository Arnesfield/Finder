<?php
// $host_name = 'http://192.168.1.10/sites/school/android/finder/web/';
$host_name = 'http://arnesfield.pe.hu/android/';
$verify_dir = 'action/verify.php';

$link = $host_name . $verify_dir . '?c=' . $verification_code;

// change here
$name = "Finder Team";

// Gmail username to be use as sender(make sure that the gmail settings was ON or enable)
$emailUsername = "jfespiritu@fit.edu.ph";

// Gmail Password used for the gmail 
$emailPassword = "---";

// return;
require("../vendor/phpmailer/PHPMailerAutoload.php");

$mail = new PHPMailer();
$mail->IsSMTP();
$mail->SMTPAuth = true;
$mail->SMTPSecure = 'ssl';
$mail->Host = "smtp.gmail.com";
$mail->Port = 465; // or 587
$mail->IsHTML(true);
$mail->Username = $emailUsername;
$mail->Password = $emailPassword;


// Name of Sender: the "FEU-IT Admin" could be change and replace as the name of the sender
$mail->SetFrom($emailUsername, $name);

// Email Subject: to get the subject from the form
$mail->Subject = 'Account Verification - Finder';

// Content of Message : to get the content or body of the email from form
$mail->Body = "Click this to activate your account: <a href='$link'>$link</a>";

// Recepient of email: to send whatever email you want to
$mail->AddAddress($email);

// mail moved to add user

// redirect
// header("location: ./");

?>