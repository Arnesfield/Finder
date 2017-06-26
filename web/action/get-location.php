<?php
if (isset($_POST['fetch']) && isset($_POST['uid'])) {
  require_once('header.php');
  require_once('db-connection.php');

  $uid = input_filter($_POST['uid']);

  $sql = "
    SELECT
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
      'username' => $row[0],
      'latitude' => $row[1],
      'longitude' => $row[2],
      'date_time' => $row[3]
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