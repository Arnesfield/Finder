<?php
if (isset($_POST['login'])) {
  require_once('header.php');
  require_once('db-connection.php');

  $username = input_filter($_POST['username']);
  $password = input_filter($_POST['password']);

  $chk_username = mysqli_real_escape_string($conn, $username);

  $query = "
    SELECT id, password, status
    FROM users
    WHERE
      username = '$chk_username' AND
      status != '0'
  ";

  $record = $conn->query($query);

  // if user does not exist
  if ($record->num_rows == 0) {
    $json_object['login'] = 0;
  }
  // if user exists
  else {
    $row = $record->fetch_assoc();

    // verify password
    $hashed_password = $row['password'];
    $is_password_match = password_verify($password, $hashed_password);

    // if verified
    if ($is_password_match) {
      // if account not verified
      if ($row['status'] == '2') {
        $json_object['login'] = -1;
      }
      // logged in
      else {
        $json_object['login'] = $row['id'];
      }
    }
    // if not
    else {
      $json_object['login'] = 0;
    }
  }

  echo json_encode($json_object);
  $conn->close();
}

?>