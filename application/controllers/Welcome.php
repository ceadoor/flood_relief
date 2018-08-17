<?php

defined('BASEPATH') OR exit('No direct script access allowed');

class Welcome extends CI_Controller {

    private $ALLOWED_KEYS;

    /**
     * Index Page for this controller.
     *
     * Maps to the following URL
     * 		http://example.com/index.php/welcome
     * 	- or -
     * 		http://example.com/index.php/welcome/index
     * 	- or -
     * Since this controller is set as the default controller in
     * config/routes.php, it's displayed at http://example.com/
     *
     * So any other public methods not prefixed with an underscore will
     * map to /index.php/welcome/<method_name>
     * @see https://codeigniter.com/user_guide/general/urls.html
     */
    public function __construct() {
        parent::__construct();
        $this->ALLOWED_KEYS = $this->config->item('api_keys');
    }

    public function index() {
        $this->load->view('welcome_message');
    }

    function set_json_output($data) {
        $ci = & get_instance();
        $ci->output->set_content_type('application/json');
        $ci->output->set_output(json_encode($data));
    }

    function check_key() {
        if ($this->input->post('key')) {
            return in_array($this->input->post('key'), $this->ALLOWED_KEYS);
        }
        return FALSE;
    }

    public function send_request() {

        if ($this->check_key()) {
            $data = ['status' => 0, 'msg' => 'Some error occured'];
            $this->load->library('form_validation');
            $this->form_validation->set_rules('name', 'Name', 'trim|strip_tags|required');
            $this->form_validation->set_rules('descr', 'Description', 'trim|strip_tags');
            $this->form_validation->set_rules('phone', 'Phone number', 'trim|strip_tags|required');
            $this->form_validation->set_rules('priority', 'Priority', 'trim|strip_tags|integer|required');
            $this->form_validation->set_rules('latitude', 'Latitude', 'trim|strip_tags|required');
            $this->form_validation->set_rules('longitude', 'Longitude', 'trim|strip_tags|required');
            if ($this->form_validation->run() == TRUE) {
                $this->db->insert('req', [
                    'name' => $this->input->post('name'),
                    'descr' => $this->input->post('descr'),
                    'phone' => $this->input->post('phone'),
                    'priority' => $this->input->post('priority'),
                    'latitude' => $this->input->post('latitude'),
                    'longitude' => $this->input->post('longitude')
                ]);
                $data = ['status' => 1, 'msg' => 'Request Successfuly posted.'];
            } else {
                $data['msg'] = validation_errors(NULL, NULL);
            }

            $this->set_json_output($data);
        }
    }

    public function setup() {
        $this->db->query("CREATE TABLE `req` (
            `ID` int(10) UNSIGNED NOT NULL,
            `name` varchar(255) NOT NULL,
            `phone` varchar(255) NOT NULL,
            `priority` tinyint(4) NOT NULL,
            `latitude` decimal(12,7) NOT NULL,
            `longitude` decimal(12,7) NOT NULL,
            `descr` text NOT NULL,
            `status` tinyint(4) NOT NULL DEFAULT '1',
            `added_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
          ) ENGINE=InnoDB DEFAULT CHARSET=utf8;");

        $this->db->query("ALTER TABLE `req`
        ADD PRIMARY KEY (`ID`);");

        $this->db->query("ALTER TABLE `req`
        MODIFY `ID` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;");
    }

    public function get_req() {

        if ($this->check_key()) {
            $latitude = $this->db->escape($this->input->post('latitude'));
            $longitude = $this->db->escape($this->input->post('longitude'));

            $extra_qstr = "";
            if ($latitude != NULL && $longitude != NULL) {
                $extra_qstr .= " (SQRT(POWER((latitude-" . $latitude . "),2))+SQRT(POWER((longitude-" . $longitude . "),2))) ASC, ";
            }

            $this->set_json_output([
                'status' => 1,
                'requests' => $this->db->query("SELECT *, (
    CASE 
        WHEN `priority` = 1 THEN 'Low'
        WHEN `priority` = 2 THEN 'Medium'
        WHEN `priority` = 3 THEN 'High'
    END) AS `priority`,DATE_FORMAT(added_on, '%d %M %y %h:%i  %p') AS added_on FROM `req` WHERE status=1 ORDER BY " . $extra_qstr . " added_on DESC")->result(),
                'msg' => 'Done'
            ]);
        }
    }

}
