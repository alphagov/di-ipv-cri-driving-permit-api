#!/usr/bin/env bash
set -e

stack_name="$1"

if [ -z "$stack_name" ]
then
echo -e "Stack name expected as first argument, e.g. ./deploy.sh my-driving-permit-api"
exit 1
fi

if [ -z "$audit_event_name_prefix" ]
then
  audit_event_name_prefix="/common-cri-parameters/DrivingPermitAuditEventNamePrefix"
fi

if [ -z "$cri_identifier" ]
then
  cri_identifier="/common-cri-parameters/DrivingPermitCriIdentifier"
fi

./gradlew clean

sam validate -t infrastructure/lambda/template.yaml --config-env dev

sam build -t infrastructure/lambda/template.yaml --config-env dev

sam deploy --stack-name $stack_name \
   --no-fail-on-empty-changeset \
   --no-confirm-changeset \
   --resolve-s3 \
   --region eu-west-2 \
   --capabilities CAPABILITY_IAM \
   --parameter-overrides CodeSigningEnabled=false Environment=dev AuditEventNamePrefix=/common-cri-parameters/DrivingPermitAuditEventNamePrefix CriIdentifier=/common-cri-parameters/DrivingPermitCriIdentifier CommonStackName=driving-permit-common-cri-api-local
