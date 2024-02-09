#!/bin/bash

#This script pushes the needed application artifacts to the given environment.

set -e

environment=$1
localProjectFolder=$2
viewVersion=$3

deploymentBucket="bloip-deployment-${environment}"
preDeploymentBucket="bloip-pre-deployment-${environment}"

#Move static secrets and environment config files to deployment bucket. The preDeploymentBucket is populated manually.
# And its contents are not meant to change across deployments, such as password files and configurations not under source control.
aws s3 cp s3://${preDeploymentBucket} s3://${deploymentBucket} --recursive

#build application
${localProjectFolder}/gradlew clean
${localProjectFolder}/gradlew build -x test

#push the key deployment files to the deployment bucket. The EC2 instance the app will run on will initialize itself
# by pulling the artifacts below onto itself, moving them to the proper locations, and running them as needed.
aws s3 cp ${localProjectFolder}/build/libs/*SNAPSHOT.jar s3://${deploymentBucket}/bloip.jar
aws s3 cp ${localProjectFolder}/src/main/resources/bloip.conf s3://${deploymentBucket}/bloip.conf
aws s3 cp ${localProjectFolder}/src/main/resources/bloip.service s3://${deploymentBucket}/bloip.service

echo "Artifacts deployed. Ready for application stack create/update command"