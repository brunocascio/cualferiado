    <?php
/**
 * Step 1: Require the Slim Framework
 *
 * If you are not using Composer, you need to require the
 * Slim Framework and register its PSR-0 autoloader.
 *
 * If you are using Composer, you can skip this step.
 */
require 'Slim/Slim.php';

\Slim\Slim::registerAutoloader();


/**
 * Step 2: Instantiate a Slim application
 *
 * This example instantiates a Slim application using
 * its default settings. However, you will usually configure
 * your Slim application now by passing an associative array
 * of setting names and values into the application constructor.
 */
$app = new \Slim\Slim();


ini_set('date.timezone', 'America/Argentina/Buenos_Aires');


/*
| -------------------------------------------------------
|   Setea el encabezado JSON para todas las respuestas
| -------------------------------------------------------
|
*/
$app->response->headers->set('Content-Type', 'application/json; charset=utf-8');


/*
| -------------------------------------------------------
|   Página 404
| -------------------------------------------------------
|
*/
$app->notFound(function () use ($app) {
    echo json_encode(array(
        'status' => 'Not found'
    ));
});


/*
| -------------------------------------------------------
|   Inyecciòn de Dependencias
| -------------------------------------------------------
|
*/
$app->feriados = file_get_contents('data/2014.json', false, null);


/*
| -------------------------------------------------------
|   Página de Bienvenida
| -------------------------------------------------------
|
*/
$app->get('/', function( ){
    echo "Hum... I haven't index :( "
        ."Could you create an index for me, please? :) ==> "
        .'<a href="https://github.com/brunocascio/cualferiado">GITHUB</a> ';
});


/*
| -------------------------------------------------------
|   Chequea si no está desactualizado el JSON de feriado
| -------------------------------------------------------
|
*/
$app->get('/check', function() {

	echo json_encode(array(
		'status' => filemtime('data/2014.json')
	));
    
});

/*
| -------------------------------------------------------
|   Retorna el JSON de feriados
| -------------------------------------------------------
|
*/
$app->get('/feriados', function() use ($app){

	$app->etag('unique-resource-id');
	$app->expires('+1 week');

	echo $app->feriados;

});



$app->run();
