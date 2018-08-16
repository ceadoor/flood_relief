<?php

defined('BASEPATH') OR exit('No direct script access allowed');

class Welcome extends CI_Controller {

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
    public function index() {
        $this->load->view('welcome_message');
    }

    function set_json_output($data) {
        $ci = & get_instance();
        $ci->output->set_content_type('application/json');
        $ci->output->set_output(json_encode($data));
    }

    public function send_request() {
        $data = $_POST;
        unset($data['key']);
        $this->db->insert('req', $data);
        $this->set_json_output([
            'status' => 1,
            'msg' => 'Done'
        ]);
    }

    public function setup(){
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
        $this->set_json_output([
            'status' => 1,
            'requests' => $this->db->where('status', 1)->get('req')->result(),
            'msg' => 'Done'
        ]);
    }

}
