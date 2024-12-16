import boto3
import sys
import os

def fetch_ips(INSTANCE_ATTRIBUTE, region='us-east-2'):
    ec2 = boto3.client('ec2', region_name=region)
    response = ec2.describe_instances(Filters=[{'Name': 'instance-state-name', 'Values': ['running']}])

    instances_info = []

    for reservation in response['Reservations']:
        for instance in reservation['Instances']:
            instance_name = next((tag['Value'] for tag in instance.get('Tags', []) if tag['Key'] == 'Name'), None)
            if INSTANCE_ATTRIBUTE == 'DT':
                if instance_name and 'BASTION' in instance_name:
                    public_ip = instance.get('PublicIpAddress', 'N/A')
                    private_ip = instance.get('PrivateIpAddress', 'N/A')
                    instances_info.append((instance_name, public_ip, private_ip))
            else:
                if instance_name and instance_name.startswith(INSTANCE_ATTRIBUTE):
                    public_ip = instance.get('PublicIpAddress', 'N/A')
                    private_ip = instance.get('PrivateIpAddress', 'N/A')
                    instances_info.append((instance_name, public_ip, private_ip))

    return sorted(instances_info, key=lambda x: x[0])

def print_table(instances_info):
    if not instances_info:
        print("No running instances found.")
        return

    print(f"{'Instance Name':<30} {'Public IP':<20} {'Private IP':<20}")
    print("-" * 70)

    for instance_name, public_ip, private_ip in instances_info:
        print(f"{instance_name:<30} {public_ip:<20} {private_ip:<20}")

def main():
    INSTANCE_ATTRIBUTE = os.getenv('EnvironmentGroup')
    instance_attribute = os.getenv('INSTANCE_ATTRIBUTE')

    #INSTANCE_ATTRIBUTE = sys.argv[1]
    #instance_attribute = sys.argv[2]

    if instance_attribute != 'fetch_ips':
        print("Invalid instance attribute.")
        sys.exit(1)

    INSTANCE_ATTRIBUTE = os.getenv('EnvironmentGroup')
    instances_info = fetch_ips(INSTANCE_ATTRIBUTE)
    print_table(instances_info)

if __name__ == '__main__':
    main()
