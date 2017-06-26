<?php
if (isset($_POST['location']) && isset($_POST['uid'])) {
  require_once('header.php');
  require_once('db-connection.php');

  $uid = input_filter($_POST['uid']);
  $latitude = input_filter($_POST['latitude']);
  $longitude = input_filter($_POST['longitude']);

  $sql = "
    INSERT INTO locations(
      user_id, latitude, longitude, date_time
    ) VALUES(?, ?, ?, CONCAT(CURRENT_DATE(), ' ', CURRENT_TIME()));
  ";

  $stmt = $conn->prepare($sql);
  $stmt->bind_param('idd', $uid, $latitude, $longitude);
  $stmt->execute();
  $stmt->close();

  $conn->close();
}
?>