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
$app->anio_actual = date("Y");
$app->feriados = file_get_contents('data/'.$app->anio_actual.'.json', false, null);


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

/*
| -------------------------------------------------------
|   Retorna el JSON de feriados para un año determinado
| -------------------------------------------------------
|
*/
$app->get('/feriados/:year', function($year) use ($app){

	$file = 'data/'.$year.'.json';

	if ( file_exists($file) ) {
		echo file_get_contents($file, false, null);
	} else {
		$app->notFound();
	}
});


/*
| -------------------------------------------------------
|   Retorna el próximo de feriado
| -------------------------------------------------------
|
*/
$app->get('/proximo', function() use ($app) {

    $others = $app->request()->params('others');
    $others = ($others && $others =='true');

    $arrayF = json_decode($app->feriados, true);

    // Hoy
    $dia        = date('d');
    $mes        = date('m');
    $anio       = date('Y');

    // variables para el manejo de la estructura
    $i          = 0;
    $total      = count($arrayF); 
    $encontrado = false;


    // Busco el próximo feriado
    while ( ($total > 0) && ($i < $total) && !($encontrado) ) {

        $f = $arrayF[$i];

        // Feriado dentro del mes actual, luego del día de hoy ó del mes siguiente
        if ( ($f['mes'] == $mes) && ($f['dia'] >= $dia) || ( $f['mes'] > $mes ) ) {
            
            $encontrado   = true;
            $fechaFeriado = array('anio' => (int) date('Y'), 'mes' => $f['mes'], 'dia' => $f['dia']);

            // Verifica si el feriado es de alguna religion o origen específico
            $isOther = ( array_key_exists('opcional', $f) && 
                        ( ( $f['opcional']['tipo'] == 'religion' && 
                            $f['opcional']['religion'] != 'cristianismo') || 
                            $f['opcional']['tipo']  == 'origen') );

            // Compara si se desean o no los otros feriados
            if ( $isOther && !$others ) {
                $encontrado = false;
            }
        }

        $i++;
    }

    // Año nuevo como fallback (por ejemplo si hoy es 27 de diciembre)
    if ( !$encontrado )
        $fechaFeriado =  array('anio' => (int) date('Y') + 1, 'mes' => '01', 'dia' => '01');
    

    // Retorno feriado
    echo json_encode(array(
        "fecha" => $fechaFeriado
    ));

});



$app->run();
