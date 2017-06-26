<?php
if (isset($_POST['location']) && isset($_POST['uid'])) {
  require_once('header.php');
  require_once('db-connection.php');

  $uid = input_filter($_POST['uid']);
  $latitude = input_filter($_POST['latitude']);
  $longitude = input_filter($_POST['longitude']);

  $sql = "
    UPDATE locations
    SET
      latitude = ?,
      longitude = ?,
      status = '1',
      date_time = CONCAT(CURRENT_DATE(), ' ', CURRENT_TIME())
    WHERE user_id = ?
  ";

  $stmt = $conn->prepare($sql);
  $stmt->bind_param('ddi', $latitude, $longitude, $uid);
  $stmt->execute();
  $stmt->close();

  $conn->close();
}
?>