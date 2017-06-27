<?php
if (isset($_POST['notify']) && isset($_POST['uid']) && !empty($_POST['send_to'])) {
  require_once('header.php');
  require_once('db-connection.php');

  $uid_from = input_filter($_POST['uid']);
  $sent_to = explode(',', input_filter($_POST['send_to']));

  $sql = "
    INSERT INTO notifications(
      uid_from, uid_to, status, date_time
    ) VALUES(?, ?, '1', CONCAT(CURRENT_DATE(), ' ', CURRENT_TIME()));
  ";

  foreach ($send_to as $uid_to) {
    $stmt = $conn->prepare($sql);
    $stmt->bind_param('ii', $uid_from, $uid_to);
    $stmt->execute();
    $stmt->close();
  }

  $conn->close();
}
else {
  // redirect
  header('location: ./');
}
?>