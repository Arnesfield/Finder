<?php
require_once('header.php');

if (isset($_POST['signup'])) {
  require_once('db-connection.php');

  // clear session first
  
  $username = input_filter($_POST['username']);
  $password = input_filter($_POST['password']);
  $email = input_filter($_POST['email']);
  
  // use this to hash passwords to insert in database
  // $hashed_password = password_hash($password, PASSWORD_DEFAULT);
  
  $chk_username = mysqli_real_escape_string($conn, $username);
  $chk_email = mysqli_real_escape_string($conn, $email);
  
  $query = "
    SELECT username FROM users
    WHERE username = '$chk_username' OR email = '$chk_email'
  ";
  
  $query_result = $conn->query($query);
  
  // if no same username or email
  if ($query_result->num_rows == 0) {
    // prepare and bind
    $query = "
      INSERT INTO users(
        username, password, email,
        verification_code, status
      ) VALUES(?, ?, ?, ?, '2');
    ";
    
    $stmt = $conn->prepare($query);
    $stmt->bind_param("ssss",
      $username, $hashed_password, $email, $verification_code
    );

    // set parameters and execute
    $hashed_password = password_hash($password, PASSWORD_DEFAULT);
    // create verification code
    $verification_code = md5(uniqid(rand(), true));
    
    // send verification code
    require_once('send-verification.php');
    
    $stmt->execute();
    
    // close statement
    $stmt->close();

    echo 'Success';
    // redirect to index
    // header('location: ./');
  }
  // check if username is taken
  else {
    /*
    $query = "SELECT username FROM users WHERE username = '$username'";
    $query_result = $conn->query($query);

    // if username exists, error
    if ($query_result->num_rows > 0)
      setcookie('msg_duplicate_username', 1, time()+60, '/');
    
    $query = "SELECT username FROM users WHERE email = '$email'";
    $query_result = $conn->query($query);
    
    // if duplicate email
    if ($query_result->num_rows > 0)
      setcookie('msg_duplicate_email', 1, time()+60, '/');
    */

    echo 'An error occured.';
  }
  
  // close connection
  $conn->close();
}
else {
  // redirect to page
  header("location: ./");
}

?>