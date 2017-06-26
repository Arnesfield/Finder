<?php
if (isset($_POST['logout']) && isset($_POST['uid'])) {
  require_once('header.php');
  require_once('db-connection.php');

  $uid = input_filter($_POST['uid']);

  $sql = "
    UPDATE locations
    SET
      status = '0',
      date_time = CONCAT(CURRENT_DATE(), ' ', CURRENT_TIME())
    WHERE user_id = ?
  ";

  $stmt = $conn->prepare($sql);
  $stmt->bind_param('i', $uid);
  $stmt->execute();
  $stmt->close();

  $conn->close();
}
?>