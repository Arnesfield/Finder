<?php

if (!isset($_GET['c']))
  return;

require_once('db-connection.php');

$code = $_GET['c'];

// based on code, query database if exists
$query = "
  SELECT status
  FROM users
  WHERE verification_code = '$code'
";

$result = $conn->query($query) or die($conn->error);

// check if exists first
if ($result->num_rows == 1) {
  $record = $result->fetch_assoc();

  $status = $record['status'];

  // verification code expired
  if ($status == '1') {
    /*// set expired code
    setcookie('msg_verification_expired', 1, time()+60, '/');

    // redirect
    header("location: ./");*/
    exit();
  }

}

// invalid code
else {
 /* // set invalid message
  setcookie('msg_verification_invalid', 1, time()+60, '/');

  // redirect
  header("location: ./");*/
  exit();
}


$sql = "
  UPDATE users
  SET status = '1'
  WHERE verification_code = '$code'
";

$result = $conn->query($sql) or die($conn->error);

// set verified message
// setcookie('msg_account_verified', 1, time()+60, '/');

// header("location: ./");
echo 'Success';

?>