<?php
$_POST['check-notif'] = '';
$_POST['uid'] = 1;
if (isset($_POST['check-notif']) && isset($_POST['uid'])) {
  require_once('header.php');
  require_once('db-connection.php');

  $uid = input_filter($_POST['uid']);

  $query = "
    SELECT
      u.id AS 'id',
      u.username AS 'username'
    FROM
      notifications n, users u
    WHERE
      n.uid_from = u.id AND
      n.uid_from != $uid AND
      n.uid_to = $uid AND
      n.status = '1'
    ORDER BY
      date_time DESC
  ";

  $result = $conn->query($query);

  $array['notifs'] = array();
  while ($row = mysqli_fetch_array($result)) {
    array_push($array['notifs'], array(
      'id' => $row[0],
      'username' => $row[1]
    ));
  }

  // encode json
  echo json_encode($array);

  // update
  $sql = "
    UPDATE notifications
    SET
      status = '0'
    WHERE
      uid_to = ?
  ";
  $stmt = $conn->prepare($sql);
  $stmt->bind_param('i', $uid);
  $stmt->execute();
  $stmt->close();

  $conn->close();
}
else {
  // redirect
  header('location: ./');
}
?>