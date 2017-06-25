<?php
require_once('db-connection.php');

$sql = "
  SELECT
    u.username AS 'username',
    l.latitude AS 'latitude',
    l.longitude AS 'longitude',
    l.date_time AS 'date_time'
  FROM
    users u, locations l
  WHERE
    u.id = l.user_id
  ORDER BY
    date_time DESC
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
?>