<?php
if (isset($_POST['fetch']) && isset($_POST['uid'])) {
  require_once('header.php');
  require_once('db-connection.php');

  $uid = input_filter($_POST['uid']);

  $sql = "
    SELECT
      u.id AS 'id',
      u.username AS 'username',
      l.latitude AS 'latitude',
      l.longitude AS 'longitude',
      l.date_time AS 'date_time'
    FROM
      users u, locations l
    WHERE
      u.id = l.user_id AND
      u.id != $uid AND
      l.status = '1'
  ";

  $result = $conn->query($sql);

  $array['locations'] = array();
  while ($row = mysqli_fetch_array($result)) {
    array_push($array['locations'], array(
      'id' => $row[0],
      'username' => $row[1],
      'latitude' => $row[2],
      'longitude' => $row[3],
      'date_time' => $row[4]
    ));
  }

  echo json_encode($array);
  $conn->close();
}
else {
  // redirect
  header('location: ./');
}
?>