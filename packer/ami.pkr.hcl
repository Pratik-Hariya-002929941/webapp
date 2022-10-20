variable "aws_source_ami" {
  type    = string
  default = env("AWS_SOURCE_AMI") # Ubuntu 22.04 LTS
}

variable "aws_region" {
  type    = string
  default = env("AWS_REGION")
}

variable "aws_subnet_id" {
  type    = string
  default = env("AWS_SUBNET_ID")
}

variable "aws_vpc_id" {
  type    = string
  default = env("AWS_VPC_ID")
}

variable "aws_ssh_username" {
  type    = string
  default = env("AWS_SSH_USERNAME")
}

variable "ami_users" {
  type = list(string)
  default = [
    env("AMI_USERS"),
  ]
}

variable "aws_access_key_id" {
  type      = string
  sensitive = true
  default   = env("AWS_ACCESS_KEY_ID")
}

variable "aws_secret_key_id" {
  type      = string
  sensitive = true
  default   = env("AWS_SECRET_KEY_ID")
}

# https://www.packer.io/plugins/builders/amazon/ebs
source "amazon-ebs" "my-ami" {
  access_key      = "${var.aws_access_key_id}"
  secret_key      = "${var.aws_secret_key_id}"
  aws_region          = "${var.aws_region}"
  ami_name        = "csye6225_${formatdate("YYYY_MM_DD_hh_mm_ss", timestamp())}"
  ami_description = "AMI for CSYE 6225"
  ami_users       = "${var.ami_users}"
  aws_vpc_id          = "${var.aws_vpc_id}"

  ami_regions = [
    "us-east-1",
  ]

  aws_polling {
    delay_seconds = 120
    max_attempts  = 50
  }


  instance_type = "t2.micro"
  aws_source_ami    = "${var.aws_source_ami}"
  aws_ssh_username  = "${var.aws_ssh_username}"
  aws_subnet_id     = "${var.aws_subnet_id}"

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/sda1"
    volume_size           = 8
    volume_type           = "gp2"
  }
}

build {
  sources = ["source.amazon-ebs.my-ami"]

  provisioner "file" {
    source      = "../SpringBootApp-0.0.1-SNAPSHOT.war"
    destination = "/tmp/SpringBootApp-0.0.1-SNAPSHOT.war"
  }

  provisioner "shell" {
    environment_vars = [
      "DEBIAN_FRONTEND=noninteractive",
      "CHECKPOINT_DISABLE=1"
    ]
    script = "./package.sh"
  }
}
